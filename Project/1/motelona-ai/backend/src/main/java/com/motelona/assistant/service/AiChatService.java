package com.motelona.assistant.service;

import com.motelona.assistant.dto.ChatRequest;
import com.motelona.assistant.dto.ChatResponse;
import com.motelona.assistant.dto.ExplainRequest;
import com.motelona.assistant.dto.MessageTurn;
import com.motelona.assistant.dto.UiAction;
import com.motelona.assistant.ai.LocalAiEngine;
import com.motelona.assistant.ai.TextVectorizer;
import com.motelona.assistant.memory.MemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;

/** Orchestrates scope checks, model failover, Portfolio/file retrieval and optional public web research. */
@Service
public class AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int MAX_MESSAGE = 3000;
    private final ModelProviderRouter modelRouter;
    private final ModelPromptBuilder promptBuilder;
    private final ScopeGuard scopeGuard;
    private final LocalAiEngine localFallback;
    private final MemoryService memories;
    private final ActionRouter actionRouter;
    private final LocalSearchService localSearch;
    private final PortfolioService portfolioService;
    private final ProjectFileSearchService projectFiles;
    private final WebResearchService webResearch;

    public AiChatService(ModelProviderRouter modelRouter, ModelPromptBuilder promptBuilder, ScopeGuard scopeGuard,
                        LocalAiEngine localFallback,
                        MemoryService memories,
                        ActionRouter actionRouter, LocalSearchService localSearch, PortfolioService portfolioService,
                        ProjectFileSearchService projectFiles, WebResearchService webResearch) {
        this.modelRouter = modelRouter;
        this.promptBuilder = promptBuilder;
        this.scopeGuard = scopeGuard;
        this.localFallback = localFallback;
        this.memories = memories;
        this.actionRouter = actionRouter;
        this.localSearch = localSearch;
        this.portfolioService = portfolioService;
        this.projectFiles = projectFiles;
        this.webResearch = webResearch;
    }

    public ChatResponse chat(ChatRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        String message = validateMessage(request.message());
        String language = language(request.language());
        String sessionId = normalizeSessionId(request.sessionId());
        boolean historyEnabled = Boolean.TRUE.equals(request.historyEnabled());
        boolean memoryEnabled = Boolean.TRUE.equals(request.memoryEnabled());

        String explicitWebQuery = extractExplicitWebSearchQuery(message);
        boolean webSearchRequested = Boolean.TRUE.equals(request.searchWeb()) || !explicitWebQuery.isBlank();
        String aiQuestion = explicitWebQuery.isBlank() ? message : explicitWebQuery;

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

        String smallTalk = fastSmallTalkReply(message, language);
        if (smallTalk != null) {
            ChatResponse result = response(smallTalk, null, false, "local");
            saveConversation(sessionId, historyEnabled, message, result.message());
            return result;
        }

        String history = conversationContext(sessionId, historyEnabled, request.history());
        if (!scopeGuard.allows(aiQuestion, history, webSearchRequested)) {
            ChatResponse result = new ChatResponse(scopeGuard.redirectMessage(language), null, false, "scope-guard", List.of());
            saveConversation(sessionId, historyEnabled, message, result.message(), result.source());
            return result;
        }

        String context = portfolioAwareContext(request.websiteContext());
        String memoryContext = memoryEnabled ? String.join("\n", memories.memoryText(sessionId, true)) : "";
        boolean projectInventory = isProjectInventoryQuestion(aiQuestion);
        List<String> fileMatches = projectFiles.search(aiQuestion).stream().limit(5).toList();
        if (!webSearchRequested) {
            String localContext = context;
            if (!fileMatches.isEmpty()) localContext += "\n\nPROJECT FILE EVIDENCE:\n" + String.join("\n", fileMatches);
            String quickAnswer = localFallback.quickAnswer(aiQuestion, language, localContext, memoryContext);
            if (quickAnswer != null) {
                ChatResponse result = response(quickAnswer, null, false, "local-knowledge-fallback");
                saveConversation(sessionId, historyEnabled, message, result.message(), result.source());
                return result;
            }
        }
        List<String> matches = localSearch.search(aiQuestion, context).stream()
                .filter(match -> match.score() >= 45)
                .limit(projectInventory ? 8 : 5).map(LocalSearchService.Match::snippet).toList();
        boolean useWebSearch = webSearchRequested && !isCasualGreeting(aiQuestion);
        WebResearchService.WebResearch research = useWebSearch
                ? webResearch.research(aiQuestion, language)
                : new WebResearchService.WebResearch("", List.of(), "disabled");
        String guidance = taskHint(aiQuestion, useWebSearch);
        if (useWebSearch && research.sources().isEmpty()) {
            guidance += " Online research was requested but returned no sources. Do not claim that current web pages were checked; explain that web search was unavailable if it affects the answer, then answer from other evidence or general knowledge.";
        }
        String focusedPageContext = projectInventory ? context : "";
        GeneratedAnswer generated;
        try {
            generated = generateAnswer(aiQuestion, language, focusedPageContext, matches, fileMatches,
                    memoryContext, history, research, guidance);
        } catch (ModelRefusalException ex) {
            String refused = refusalMessage(language);
            saveConversation(sessionId, historyEnabled, message, refused, "provider-refusal");
            return new ChatResponse(refused, null, false, "provider-refusal", List.of());
        }
        String answer = trim(generated.text(), 9000);
        saveConversation(sessionId, historyEnabled, message, answer, generated.source());
        return new ChatResponse(answer, null, generated.aiPowered(), generated.source(), research.sources());
    }

    public ChatResponse explain(ExplainRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        String language = language(request.language());
        String selectedText = trim(request.selectedText() != null && !request.selectedText().isBlank()
                ? request.selectedText() : request.term(), 2000);
        if (selectedText.isBlank()) throw new IllegalArgumentException("Provide a term or selected text to explain.");
        String level = trim(request.level(), 30);
        String context = portfolioAwareContext(request.websiteContext());
        List<String> matches = localSearch.search(selectedText, context).stream()
                .limit(12).map(LocalSearchService.Match::snippet).toList();
        List<String> fileMatches = projectFiles.search(selectedText);
        boolean useWebSearch = Boolean.TRUE.equals(request.searchWeb());
        WebResearchService.WebResearch research = useWebSearch
                ? webResearch.research(selectedText, language)
                : new WebResearchService.WebResearch("", List.of(), "disabled");
        String instruction = "Explain the selected text at a " + (level.isBlank() ? "basic" : level)
                + " level, in plain language, using a simple analogy when useful. Explain any connection to this page. "
                + (useWebSearch ? "Integrate web source evidence when useful and cite sources; explain the findings rather than listing links." : "");
        if (useWebSearch && research.sources().isEmpty()) instruction += " Online search returned no sources; do not imply that current sources were checked.";
        GeneratedAnswer generated;
        if (modelRouter.isConfigured()) {
            ModelPrompt prompt = promptBuilder.build(selectedText, language, context, matches, fileMatches,
                    "", "", research.summary(), research.sources(), instruction);
            try {
                var answer = modelRouter.answer(prompt);
                generated = new GeneratedAnswer(answer.text(), true, answer.provider());
            } catch (ModelRefusalException ex) {
                return new ChatResponse(refusalMessage(language), null, false, "provider-refusal", List.of());
            } catch (AiUnavailableException ex) {
                log.warn("All configured model providers failed for explain; using local knowledge fallback");
                generated = new GeneratedAnswer(localFallback.explain(selectedText + offlineEvidence(fileMatches, research),
                        level, language, context), false, "local-knowledge-fallback");
            }
        } else {
            generated = new GeneratedAnswer(localFallback.explain(selectedText + offlineEvidence(fileMatches, research),
                    level, language, context), false, "local-knowledge-fallback");
        }
        return new ChatResponse(trim(generated.text(), 6000), null, generated.aiPowered(), generated.source(), research.sources());
    }

    public boolean isAiConfigured() { return modelRouter.isConfigured(); }
    public boolean isChatReady() { return true; }
    public String modelName() { return modelRouter.primaryModel(); }
    public String activeProvider() { return modelRouter.primaryProvider(); }
    public List<String> configuredProviders() { return modelRouter.configuredProviders(); }
    public boolean isInternetSearchEnabled() { return webResearch.isInternetSearchEnabled(); }
    public String webSearchProvider() { return webResearch.providerDescription(); }

    private GeneratedAnswer generateAnswer(String question, String language, String pageContext, List<String> matches,
                                           List<String> fileMatches, String memoryContext, String history,
                                           WebResearchService.WebResearch research, String guidance) {
        if (modelRouter.isConfigured()) {
            ModelPrompt prompt = promptBuilder.build(question, language, pageContext, matches, fileMatches,
                    memoryContext, history, research.summary(), research.sources(), guidance);
            try {
                var result = modelRouter.answer(prompt);
                return new GeneratedAnswer(result.text(), true, result.provider());
            } catch (AiUnavailableException ex) {
                log.warn("All configured model providers failed for chat; using local knowledge fallback");
            }
        }
        String evidence = trim((pageContext == null ? "" : pageContext) + "\n\n"
                + String.join("\n", matches == null ? List.of() : matches)
                + offlineEvidence(fileMatches, research), 42000);
        return new GeneratedAnswer(localFallback.chat(question, language, evidence, memoryContext, history),
                false, "local-knowledge-fallback");
    }

    private String offlineEvidence(List<String> fileMatches, WebResearchService.WebResearch research) {
        StringBuilder evidence = new StringBuilder();
        if (fileMatches != null && !fileMatches.isEmpty()) {
            evidence.append("\n\nPROJECT FILE EVIDENCE:\n").append(String.join("\n", fileMatches));
        }
        if (research != null && research.sources() != null && !research.sources().isEmpty()) {
            evidence.append("\n\nPUBLIC WEB EVIDENCE:\n");
            for (int i = 0; i < research.sources().size(); i++) {
                var source = research.sources().get(i);
                evidence.append('[').append(i + 1).append("] ").append(source.title()).append(" — ")
                        .append(source.snippet()).append(" ( ").append(source.url()).append(" )\n");
            }
        }
        return evidence.toString();
    }

    private String refusalMessage(String language) {
        return "vi".equals(language)
                ? "Nhà cung cấp AI đã từ chối yêu cầu này. Mình sẽ không chuyển câu hỏi bị từ chối sang nhà cung cấp khác. Hãy thử đặt lại câu hỏi an toàn và liên quan đến Portfolio/dự án."
                : "The AI provider declined this request. I will not send a refused request to another provider. Please rephrase it safely and keep it related to the Portfolio or project.";
    }

    /** Compatibility endpoint for clients that request a direct page search. Chat always runs this retrieval automatically. */
    public List<LocalSearchService.Match> searchWebsite(String query, String websiteContext) {
        return localSearch.search(query, portfolioAwareContext(websiteContext));
    }

    private String taskHint(String question, boolean useWebSearch) {
        if (isProjectInventoryQuestion(question)) {
            return "The user is asking which projects appear on this Portfolio. Inspect the complete published project inventory, the page's Projects section, and relevant project files. List every distinct project supported by that evidence, with a one-sentence description when available. Do not count technologies, assistant features, or roadmap items as Portfolio projects. If no project evidence exists, say that clearly instead of guessing."
                    + (useWebSearch ? " The user also enabled web research; use it only if it adds useful, relevant information, and keep Portfolio project claims grounded in the page evidence." : "");
        }
        if (useWebSearch) {
            return "The user enabled web search. Evaluate the retrieved search evidence, compare sources where useful, combine findings with Portfolio and project-file evidence, and synthesize a direct answer with citations. Never return search links as a substitute for an answer.";
        }
        return "Answer the latest message directly and stay within this Portfolio, the MoteLona project, and their technologies. Use general knowledge only to explain these topics; the backend scope guard handles requests outside this scope unless the user enabled web research.";
    }

    private boolean isProjectInventoryQuestion(String question) {
        String normalized = TextVectorizer.normalize(question);
        return List.of("du an nao", "nhung du an", "cac du an", "liet ke du an", "danh sach du an",
                        "what projects", "which projects", "list projects", "show projects", "my projects",
                        "featured projects", "project inventory", "what have i built", "what did i build",
                        "what have you built", "show my work")
                .stream().anyMatch(normalized::contains);
    }

    private String conversationContext(String sessionId, boolean historyEnabled, List<MessageTurn> clientHistory) {
        if (historyEnabled) {
            try {
                List<MessageTurn> stored = portfolioService.recentConversation(sessionId, 12);
                if (!stored.isEmpty()) return formatHistory(stored);
            } catch (Exception ex) {
                log.warn("MongoDB conversation history unavailable; exceptionType={}", ex.getClass().getSimpleName());
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
        saveConversation(sessionId, enabled, user, assistant, "local");
    }

    private void saveConversation(String sessionId, boolean enabled, String user, String assistant, String source) {
        if (!enabled) return;
        try {
            portfolioService.archiveConversation(sessionId, trim(user, MAX_MESSAGE), trim(assistant, 9000), source);
        } catch (Exception ex) {
            log.warn("MongoDB conversation archive failed; exceptionType={}", ex.getClass().getSimpleName());
        }
    }

    private String portfolioAwareContext(String browserContext) {
        String context = trim(browserContext, 26000);
        try {
            String databaseContext = portfolioService.aiContext();
            if (databaseContext == null || databaseContext.isBlank()) return context;
            String combined = databaseContext + (context.isBlank() ? "" : "\n\nCURRENT PAGE CONTENT:\n" + context);
            // Database project summaries lead the evidence so a long page cannot crowd
            // every project name out of the model's context window.
            return trim(combined, 42000);
        } catch (Exception ex) {
            log.warn("MongoDB Portfolio context unavailable; exceptionType={}", ex.getClass().getSimpleName());
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
    private boolean isCasualGreeting(String text) {
        String normalized = TextVectorizer.normalize(text);
        return List.of("alo", "hello", "hi", "hey", "xin chao", "chao", "chao ban", "e", "yo")
                .contains(normalized);
    }

    private String fastSmallTalkReply(String text, String language) {
        String normalized = TextVectorizer.normalize(text);
        if (normalized.length() > 48) return null;
        if (List.of("chao", "xin chao", "xin chao ban", "chao ban", "chao motelona", "alo", "hi", "hello", "hey", "yo",
                "hi there", "hey there", "good morning", "good afternoon", "good evening", "are you there")
                .contains(normalized)) {
            boolean english = List.of("hello", "hi there", "hey there", "good morning", "good afternoon", "good evening", "are you there")
                    .contains(normalized) || (List.of("hi", "hey", "yo").contains(normalized) && language.equals("en"));
            return english ? "Hi! I’m here. What would you like to ask?" : "Chào bạn! Mình đây — bạn muốn hỏi gì?";
        }
        if (List.of("cam on", "cam on ban", "cam on nhe", "cam on nhieu", "thank you", "thanks", "thanks a lot", "thank you so much")
                .contains(normalized)) {
            boolean english = List.of("thank you", "thanks", "thanks a lot", "thank you so much").contains(normalized);
            return english ? "Happy to help. Feel free to ask anything else!" : "Rất vui được giúp bạn. Bạn cứ hỏi tiếp nhé!";
        }
        if (List.of("tam biet", "hen gap lai", "bye", "goodbye", "see you").contains(normalized)) {
            boolean english = List.of("bye", "goodbye", "see you").contains(normalized);
            return english ? "Goodbye! Come back whenever you need help." : "Tạm biệt bạn, khi nào cần cứ quay lại nhé!";
        }
        return null;
    }
    private String trim(String value, int max) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.length() > max ? normalized.substring(0, max) : normalized;
    }

    private record GeneratedAnswer(String text, boolean aiPowered, String source) { }

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
