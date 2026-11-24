package com.motelona.assistant.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Minimal OpenAI Chat Completions client shared by Groq and OpenRouter. */
public abstract class OpenAiCompatibleModelService implements GenerativeModelProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleModelService.class);
    private final ObjectMapper mapper;
    private final String providerName;
    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final int priority;
    private final HttpClient client;

    protected OpenAiCompatibleModelService(ObjectMapper mapper, String providerName, String apiKey,
                                           String endpoint, String model, int priority) {
        this.mapper = mapper;
        this.providerName = providerName;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.endpoint = endpoint == null ? "" : endpoint.trim();
        this.model = model == null ? "" : model.trim();
        this.priority = priority;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    @Override public String name() { return providerName; }
    @Override public int priority() { return priority; }
    @Override public boolean isConfigured() { return !apiKey.isBlank() && !endpoint.isBlank() && !model.isBlank(); }
    @Override public String modelName() { return model; }

    @Override
    public String answer(ModelPrompt prompt) {
        return answer(prompt, Duration.ofSeconds(20));
    }

    @Override
    public String answer(ModelPrompt prompt, Duration timeout) {
        if (!isConfigured()) throw new AiUnavailableException(providerName + " chưa được cấu hình.");
        Duration requestTimeout = timeout == null || timeout.isNegative() || timeout.isZero()
                ? Duration.ofSeconds(20) : timeout;
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", prompt.maxOutputTokens());
            body.put("temperature", 0.3);
            var messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", prompt.systemInstruction());
            messages.addObject().put("role", "user").put("content", prompt.userPrompt());
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Model provider {} returned HTTP {}", providerName, response.statusCode());
                throw apiError(response.statusCode());
            }
            JsonNode choice = mapper.readTree(response.body()).path("choices").path(0);
            String finishReason = choice.path("finish_reason").stringValueOpt().orElse("");
            String refusal = choice.path("message").path("refusal").stringValueOpt().orElse("");
            if ("content_filter".equalsIgnoreCase(finishReason) || "refusal".equalsIgnoreCase(finishReason)
                    || !refusal.isBlank()) {
                throw new ModelRefusalException(providerName);
            }
            String text = extractText(choice.path("message").path("content"));
            if (text.isBlank()) throw new AiUnavailableException(providerName + " không trả về nội dung văn bản.");
            return text;
        } catch (AiUnavailableException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiUnavailableException("Yêu cầu tới " + providerName + " bị gián đoạn.");
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Model provider {} request failed; exceptionType={}", providerName, ex.getClass().getSimpleName());
            throw new AiUnavailableException("Không kết nối được " + providerName + ".");
        }
    }

    private AiUnavailableException apiError(int status) {
        if (status == 401 || status == 403) return new AiUnavailableException(providerName + " từ chối API key hoặc quyền truy cập.");
        if (status == 429) return new AiUnavailableException(providerName + " đang giới hạn yêu cầu hoặc đã hết quota.");
        if (status >= 500 || status == 408) return new AiUnavailableException(providerName + " đang tạm thời gặp sự cố.");
        return new AiUnavailableException(providerName + " từ chối yêu cầu; hãy kiểm tra endpoint và tên model.");
    }

    private String extractText(JsonNode content) {
        if (content.isString()) return content.stringValueOpt().orElse("").trim();
        if (!content.isArray()) return "";
        StringBuilder text = new StringBuilder();
        for (JsonNode part : content) {
            String value = part.path("text").stringValueOpt().orElse("").trim();
            if (!value.isBlank()) {
                if (!text.isEmpty()) text.append('\n');
                text.append(value);
            }
        }
        return text.toString().trim();
    }
}
