package com.motelona.assistant.service;

import com.motelona.assistant.ai.LocalAiEngine;
import com.motelona.assistant.dto.ChatRequest;
import com.motelona.assistant.dto.ChatResponse;
import com.motelona.assistant.dto.ExplainRequest;
import com.motelona.assistant.dto.MessageTurn;
import com.motelona.assistant.dto.UiAction;
import com.motelona.assistant.memory.MemoryService;
import com.motelona.assistant.search.SearchProvider;
import com.motelona.assistant.search.SearchProvider.SearchResult;
import com.motelona.assistant.search.SearchUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Orchestrates local Java NLP, Portfolio retrieval, opt-in memory and chat history. */
@Service
public class AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int MAX_MESSAGE = 3000;
    private static final String ENGINE_NAME = LocalAiEngine.ENGINE_VERSION;
    private final LocalAiEngine localAi;
    private final JdbcTemplate jdbc;
    private final MemoryService memories;
    private final ActionRouter actionRouter;
    private final LocalSearchService localSearch;
    private final PortfolioService portfolioService;
    private final SearchProvider searchProvider;

    public AiChatService(LocalAiEngine localAi, JdbcTemplate jdbc, MemoryService memories,
                        ActionRouter actionRouter, LocalSearchService localSearch, PortfolioService portfolioService,
                        SearchProvider searchProvider) {
        this.localAi = localAi;
        this.jdbc = jdbc;
        this.memories = memories;
        this.actionRouter = actionRouter;
        this.localSearch = localSearch;
        this.portfolioService = portfolioService;
        this.searchProvider = searchProvider;
    }

    public ChatResponse chat(ChatRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        String message = validateMessage(request.message());
        String language = language(request.language());
        String sessionId = normalizeSessionId(request.sessionId());
        boolean historyEnabled = Boolean.TRUE.equals(request.historyEnabled());
        boolean memoryEnabled = Boolean.TRUE.equals(request.memoryEnabled());

        String explicitWebQuery = extractExplicitWebSearchQuery(message);
        String webQuery = Boolean.TRUE.equals(request.searchWeb()) ? (explicitWebQuery.isBlank() ? message : explicitWebQuery) : explicitWebQuery;
        if (!webQuery.isBlank()) {
            if (webQuery.length() > 200) throw new IllegalArgumentException("Internet search query must be 200 characters or fewer.");
            try {
                List<SearchResult> results = searchProvider.search(webQuery);
                String answer = webSearchAnswer(results.size(), language);
                ChatResponse result = new ChatResponse(answer, null, true, "web-search", results);
                saveConversation(sessionId, historyEnabled, message, answer);
                return result;
            } catch (SearchUnavailableException ex) {
                throw new AiUnavailableException(ex.getMessage());
            }
        }

        String rememberText = extractRememberRequest(message);
        if (rememberText != null) {
            if (!memoryEnabled) return response(language.equals("vi")
                    ? "Bộ nhớ dài hạn đang tắt. Hãy bật trong phần Quyền riêng tư & bộ nhớ rồi yêu cầu ghi nhớ lại."
                    : "Long-term memory is off. Turn it on in Privacy & memory, then ask me to remember this again.", null, false, "local");
            var saved = memories.saveExplicitly(sessionId, rememberText);
            ChatResponse result = response(language.equals("vi") ? "Đã lưu vào bộ nhớ dài hạn: " + saved.content()
                    : "Saved to long-term memory: " + saved.content(), null, false, "memory");
            saveConversation(sessionId, historyEnabled, message, result.message());
            return result;
        }

        String forgetText = extractForgetRequest(message);
        if (forgetText != null) {
            int deleted = memories.forgetMatching(sessionId, forgetText);
            ChatResponse result = response(language.equals("vi") ? "Đã xóa " + deleted + " mục bộ nhớ phù hợp."
                    : "Deleted " + deleted + " matching memory item(s).", null, false, "memory");
            saveConversation(sessionId, historyEnabled, message, result.message());
            return result;
        }

        UiAction action = actionRouter.routeExplicitNavigation(message, request.websiteContext());
        if (action != null) {
            String label = actionRouter.navigationLabel(action.target(), language);
            String term = action.term();
            String destinationText = language.equals("vi") ? "Đang chuyển đến " + label : "Taking you to " + label;
            if (term != null && !term.isBlank()) destinationText += language.equals("vi")
                    ? " và tô vàng nội dung liên quan đến “" + term + "”." : " and highlighting “" + term + "”.";
            else destinationText += ".";
            ChatResponse result = response(destinationText, action, false, "navigation");
            saveConversation(sessionId, historyEnabled, message, result.message());
            return result;
        }

        String context = portfolioAwareContext(request.websiteContext());
        String memoryContext = memoryEnabled ? String.join("\n", memories.memoryText(sessionId, true)) : "";
        String history = conversationContext(sessionId, historyEnabled, request.history());
        String answer = localAi.chat(message, language, context, memoryContext, history);
        answer = trim(answer, 9000);
        saveConversation(sessionId, historyEnabled, message, answer);
        return response(answer, null, true, "local-ai");
    }

    public ChatResponse explain(ExplainRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        String language = language(request.language());
        String selectedText = trim(request.selectedText() != null && !request.selectedText().isBlank()
                ? request.selectedText() : request.term(), 2000);
        if (selectedText.isBlank()) throw new IllegalArgumentException("Provide a term or selected text to explain.");
        String level = trim(request.level(), 30);
        String context = portfolioAwareContext(request.websiteContext());
        String answer = localAi.explain(selectedText, level, language, context);
        return response(trim(answer, 6000), null, true, "local-ai");
    }

    public List<LocalSearchService.Match> searchWebsite(String query, String context) {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Search query is required.");
        if (query.length() > 250) throw new IllegalArgumentException("Search query must be 250 characters or fewer.");
        return localSearch.search(query, trim(context, 9000));
    }

    private String conversationContext(String sessionId, boolean historyEnabled, List<MessageTurn> clientHistory) {
        if (historyEnabled) {
            try {
                List<MessageTurn> stored = jdbc.query(
                        "SELECT role, content FROM conversation_messages WHERE session_id = ? ORDER BY id DESC LIMIT 12",
                        (rs, row) -> new MessageTurn(rs.getString("role"), rs.getString("content")), sessionId);
                if (!stored.isEmpty()) { Collections.reverse(stored); return formatHistory(stored); }
            } catch (Exception ex) {
                log.warn("MySQL conversation history unavailable; exceptionType={}", ex.getClass().getSimpleName());
            }
        }
        if (clientHistory == null || clientHistory.isEmpty()) return "(No earlier turns.)";
        return formatHistory(clientHistory.stream().filter(m -> m != null && List.of("user", "assistant").contains(m.role()))
                .limit(12).map(m -> new MessageTurn(m.role(), trim(m.content(), 1200))).toList());
    }

    private String formatHistory(List<MessageTurn> turns) {
        StringBuilder out = new StringBuilder();
        for (MessageTurn turn : turns) if (turn != null && turn.content() != null)
            out.append(turn.role()).append(": ").append(trim(turn.content(), 1200)).append("\n");
        return out.isEmpty() ? "(No earlier turns.)" : out.toString();
    }

    private void saveConversation(String sessionId, boolean enabled, String user, String assistant) {
        if (!enabled) return;
        try {
            jdbc.update("INSERT INTO conversation_messages(session_id, role, content) VALUES (?, ?, ?)", sessionId, "user", trim(user, MAX_MESSAGE));
            jdbc.update("INSERT INTO conversation_messages(session_id, role, content) VALUES (?, ?, ?)", sessionId, "assistant", trim(assistant, 9000));
            jdbc.update("DELETE FROM conversation_messages WHERE session_id = ? AND id < (SELECT cutoff_id FROM (SELECT id AS cutoff_id FROM conversation_messages WHERE session_id = ? ORDER BY id DESC LIMIT 1 OFFSET 199) AS retained)", sessionId, sessionId);
            portfolioService.archiveConversation(sessionId, trim(user, MAX_MESSAGE), trim(assistant, 9000), ENGINE_NAME);
        } catch (Exception ex) {
            log.warn("MySQL conversation archive failed; exceptionType={}", ex.getClass().getSimpleName());
        }
    }

    private String portfolioAwareContext(String browserContext) {
        String context = trim(browserContext, 9000);
        try {
            String databaseContext = portfolioService.aiContext();
            if (databaseContext == null || databaseContext.isBlank()) return context;
            String combined = (context.isBlank() ? "" : context + "\n\n") + databaseContext;
            return trim(combined, 14000);
        } catch (Exception ex) {
            log.warn("MySQL Portfolio context unavailable; exceptionType={}", ex.getClass().getSimpleName());
            return context;
        }
    }

    private ChatResponse response(String message, UiAction action, boolean ai, String source) {
        return new ChatResponse(message, action, ai, source, List.of());
    }

    private String extractExplicitWebSearchQuery(String message) {
        String[] prefixes = {
                "(?iu)^\\s*t[iì]m(?: ki[eế]m)?\\s+(?:tr[eê]n\\s+)?(?:internet|m[aạ]ng|web)\\s*[:\\-]?\\s*(?:v[eề]\\s+)?",
                "(?iu)^\\s*tra c[uứ]u\\s+(?:tr[eê]n\\s+)?(?:internet|m[aạ]ng|web)\\s*[:\\-]?\\s*(?:v[eề]\\s+)?",
                "(?iu)^\\s*search\\s+(?:the\\s+)?(?:web|internet)(?:\\s+for)?\\s*[:\\-]?\\s*",
                "(?iu)^\\s*look\\s+up(?:\\s+online)?\\s*[:\\-]?\\s*"
        };
        for (String prefix : prefixes) {
            String query = message.replaceFirst(prefix, "").trim();
            if (!query.equals(message.trim())) return query;
        }
        return "";
    }

    private String webSearchAnswer(int count, String language) {
        if ("vi".equals(language)) return "Tìm thấy " + count + " nguồn công khai. Bên dưới là tiêu đề và trích đoạn do DuckDuckGo trả về; hãy mở nguồn để kiểm tra ngày đăng, ngữ cảnh và độ tin cậy trước khi áp dụng thông tin.";
        return "Found " + count + " public sources. The titles and snippets below come from DuckDuckGo; open each source to check its date, context, and reliability before applying the information.";
    }
    private String validateMessage(String message) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message must not be empty.");
        String value = message.trim();
        if (value.length() > MAX_MESSAGE) throw new IllegalArgumentException("Message must be 3000 characters or fewer.");
        return value;
    }
    private String normalizeSessionId(String value) {
        String id = value == null ? "" : value.trim();
        if (!id.matches("[A-Za-z0-9._-]{4,100}")) throw new IllegalArgumentException("Invalid session ID format.");
        return id;
    }
    private String language(String value) { return "vi".equalsIgnoreCase(value) ? "vi" : "en"; }
    private String trim(String value, int max) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.length() > max ? normalized.substring(0, max) : normalized;
    }

    private String extractRememberRequest(String text) {
        String low = text.toLowerCase(Locale.ROOT).trim();
        String[] prefixes = {"hãy nhớ rằng", "hãy ghi nhớ", "ghi nhớ rằng", "hãy nhớ:", "remember that", "please remember that", "remember:", "save this memory:"};
        for (String prefix : prefixes) if (low.startsWith(prefix)) {
            String rest = text.substring(prefix.length()).trim(); return rest.isBlank() ? null : rest;
        }
        return null;
    }
    private String extractForgetRequest(String text) {
        String low = text.toLowerCase(Locale.ROOT).trim();
        String[] all = {"hãy quên tất cả", "xóa toàn bộ bộ nhớ", "xóa tất cả bộ nhớ", "forget all memories", "forget everything you remember", "delete all memories"};
        for (String prefix : all) if (low.startsWith(prefix)) return "all";
        String[] prefixes = {"hãy quên rằng", "hãy quên", "xóa ghi nhớ về", "xóa bộ nhớ về", "forget that", "forget memory about", "forget about", "delete memory about"};
        for (String prefix : prefixes) if (low.startsWith(prefix)) {
            String rest = text.substring(prefix.length()).trim(); return rest.isBlank() ? null : rest;
        }
        return null;
    }
}
