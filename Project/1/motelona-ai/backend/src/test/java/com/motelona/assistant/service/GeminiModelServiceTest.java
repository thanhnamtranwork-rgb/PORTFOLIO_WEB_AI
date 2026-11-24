package com.motelona.assistant.service;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiModelServiceTest {
    @Test
    void sendsGeminiGenerateContentRequestAndReturnsOnlyNonThoughtText() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> apiKeyHeader = new AtomicReference<>();
        server.createContext("/v1beta/models/gemini-3.8-flash:generateContent", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            apiKeyHeader.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            byte[] response = """
                    {"candidates":[{"content":{"role":"model","parts":[
                      {"text":"private thought","thought":true},
                      {"text":"Useful answer."}
                    ]}}]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) { output.write(response); }
        });
        server.start();

        try {
            String endpoint = "http://127.0.0.1:" + server.getAddress().getPort()
                    + "/v1beta/models/{model}:generateContent";
            GeminiModelService service = new GeminiModelService(new ObjectMapper(), "test-key", endpoint, "gemini-3.8-flash");

            String answer = service.answer("Explain Gemini briefly", "en", "", List.of(), List.of(),
                    "", "", "", List.of(), "");

            var sent = new ObjectMapper().readTree(requestBody.get());
            assertEquals("test-key", apiKeyHeader.get());
            assertFalse(requestBody.get().contains("test-key"));
            assertTrue(sent.path("systemInstruction").path("parts").get(0).path("text").isString());
            assertEquals("user", sent.path("contents").get(0).path("role").stringValue());
            assertTrue(sent.path("contents").get(0).path("parts").get(0).path("text").stringValue()
                    .contains("Explain Gemini briefly"));
            assertEquals(384, sent.path("generationConfig").path("maxOutputTokens").asInt());
            assertEquals("Useful answer.", answer);
        } finally {
            server.stop(0);
        }
    }
}
