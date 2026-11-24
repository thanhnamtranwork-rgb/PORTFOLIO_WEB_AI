package com.motelona.assistant.service;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatibleModelServiceTest {
    @Test void sendsBearerAuthenticatedChatCompletionAndReadsAssistantText() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> auth = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext("/chat/completions", exchange -> {
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] payload = "{\"choices\":[{\"message\":{\"content\":\"Groq backup answer\"}}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
        });
        server.start();
        try {
            String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/chat/completions";
            var provider = new GroqModelService(new ObjectMapper(), "test-key", endpoint, "test-model");

            String answer = provider.answer(new ModelPrompt("system prompt", "user prompt", 256));

            assertEquals("Groq backup answer", answer);
            assertEquals("Bearer test-key", auth.get());
            assertFalse(body.get().contains("test-key"));
            var request = new ObjectMapper().readTree(body.get());
            assertEquals("test-model", request.path("model").stringValue());
            assertEquals("system", request.path("messages").get(0).path("role").stringValue());
            assertEquals("user prompt", request.path("messages").get(1).path("content").stringValue());
        } finally {
            server.stop(0);
        }
    }
}
