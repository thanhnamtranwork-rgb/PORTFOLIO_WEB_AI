package com.motelona.assistant.config;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiSecurityHeadersFilterTest {
    @Test
    void rejectsOversizedRequestWithoutDeclaredContentLength() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ai/chat") {
            @Override public int getContentLength() { return -1; }
            @Override public long getContentLengthLong() { return -1; }
        };
        request.setContent(new byte[64 * 1024 + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<byte[]> receivedBody = new AtomicReference<>();

        new ApiSecurityHeadersFilter().doFilter(request, response,
                (filteredRequest, filteredResponse) -> receivedBody.set(filteredRequest.getInputStream().readAllBytes()));

        assertEquals(413, response.getStatus());
        assertNull(receivedBody.get());
    }

    @Test
    void replaysSmallRequestWithoutDeclaredContentLength() throws Exception {
        byte[] body = "hello MoteLona".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ai/chat") {
            @Override public int getContentLength() { return -1; }
            @Override public long getContentLengthLong() { return -1; }
        };
        request.setContent(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<byte[]> receivedBody = new AtomicReference<>();

        new ApiSecurityHeadersFilter().doFilter(request, response,
                (filteredRequest, filteredResponse) -> receivedBody.set(filteredRequest.getInputStream().readAllBytes()));

        assertEquals(200, response.getStatus());
        assertArrayEquals(body, receivedBody.get());
    }
}
