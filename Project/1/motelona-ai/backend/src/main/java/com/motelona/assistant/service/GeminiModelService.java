package com.motelona.assistant.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Calls Google's Gemini generateContent API from the server; credentials never reach the browser. */
@Service
public class GeminiModelService implements GenerativeModelProvider {
    private static final Logger log = LoggerFactory.getLogger(GeminiModelService.class);
    private final ObjectMapper mapper;
    private final HttpClient client;
    private final String apiKey;
    private final String endpointTemplate;
    private final String model;

    public GeminiModelService(ObjectMapper mapper,
                              @Value("${motelona.gemini.api-key:}") String apiKey,
                              @Value("${motelona.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent}") String endpoint,
                              @Value("${motelona.gemini.model:gemini-3.8-flash}") String model) {
        this.mapper = mapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.endpointTemplate = endpoint == null || endpoint.isBlank()
                ? "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent" : endpoint.trim();
        this.model = model == null || model.isBlank() ? "gemini-3.8-flash" : model.trim();
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    @Override public String name() { return "gemini"; }
    @Override public int priority() { return 10; }
    @Override public boolean isConfigured() { return !apiKey.isBlank(); }
    @Override public String modelName() { return model; }

    @Override
    public String answer(ModelPrompt prompt) {
        return answer(prompt, Duration.ofSeconds(20));
    }

    @Override
    public String answer(ModelPrompt prompt, Duration timeout) {
        if (!isConfigured()) throw new AiUnavailableException("Gemini chưa được cấu hình.");
        Duration requestTimeout = timeout == null || timeout.isNegative() || timeout.isZero()
                ? Duration.ofSeconds(20) : timeout;
        ObjectNode body = mapper.createObjectNode();
        body.putObject("systemInstruction").putArray("parts").addObject().put("text", prompt.systemInstruction());
        body.putArray("contents").addObject().put("role", "user").putArray("parts")
                .addObject().put("text", prompt.userPrompt());
        body.putObject("generationConfig").put("maxOutputTokens", prompt.maxOutputTokens());
        try {
            String endpoint = endpointTemplate.replace("{model}", URLEncoder.encode(model, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(requestTimeout)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Gemini generateContent API returned HTTP {}", response.statusCode());
                throw apiError(response.statusCode());
            }
            String answer = extractOutputText(mapper.readTree(response.body()));
            if (answer.isBlank()) throw new AiUnavailableException("Gemini chưa trả về nội dung văn bản.");
            return answer;
        } catch (AiUnavailableException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiUnavailableException("Yêu cầu tới Gemini bị gián đoạn.");
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Gemini request failed; exceptionType={}", ex.getClass().getSimpleName());
            throw new AiUnavailableException("Không kết nối được Gemini.");
        }
    }

    /** Keeps the existing direct-call API available for small integrations and tests. */
    public String answer(String question, String language, String pageContext, java.util.List<String> localMatches,
                         java.util.List<String> projectFileMatches, String memoryContext, String conversationHistory,
                         String webResearchSummary, java.util.List<com.motelona.assistant.search.SearchProvider.SearchResult> webResults,
                         String taskHint) {
        ModelPrompt prompt = new ModelPromptBuilder().build(question, language, pageContext, localMatches,
                projectFileMatches, memoryContext, conversationHistory, webResearchSummary, webResults, taskHint);
        return answer(prompt);
    }

    private AiUnavailableException apiError(int status) {
        if (status == 401 || status == 403) return new AiUnavailableException("Gemini từ chối API key hoặc quyền truy cập.");
        if (status == 429) return new AiUnavailableException("Gemini đang giới hạn yêu cầu hoặc đã hết quota.");
        if (status >= 500 || status == 408) return new AiUnavailableException("Gemini đang tạm thời gặp sự cố.");
        return new AiUnavailableException("Gemini từ chối yêu cầu; hãy kiểm tra model và endpoint.");
    }

    private String extractOutputText(JsonNode response) {
        StringBuilder output = new StringBuilder();
        JsonNode candidates = response.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode candidate = candidates.get(0);
            String finishReason = candidate.path("finishReason").stringValueOpt().orElse("");
            if ("SAFETY".equalsIgnoreCase(finishReason) || "RECITATION".equalsIgnoreCase(finishReason)) {
                throw new ModelRefusalException(name());
            }
            JsonNode parts = candidate.path("content").path("parts");
            if (parts.isArray()) {
                for (JsonNode part : parts) {
                    if (part.path("thought").asBoolean()) continue;
                    String text = part.path("text").stringValueOpt().orElse("").trim();
                    if (!text.isBlank()) {
                        if (!output.isEmpty()) output.append('\n');
                        output.append(text);
                    }
                }
            }
        }
        return output.toString().trim();
    }
}
