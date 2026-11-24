package com.motelona.assistant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiSecurityHeadersFilter extends OncePerRequestFilter {
    private static final long MAX_DECLARED_API_BODY_BYTES = 64L * 1024L;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            if (request.getContentLengthLong() > MAX_DECLARED_API_BODY_BYTES) {
                response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "API request body exceeds 64 KB.");
                return;
            }
            if (request.getContentLengthLong() < 0) {
                byte[] body = request.getInputStream().readNBytes((int) MAX_DECLARED_API_BODY_BYTES + 1);
                if (body.length > MAX_DECLARED_API_BODY_BYTES) {
                    response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "API request body exceeds 64 KB.");
                    return;
                }
                request = new CachedBodyRequest(request, body);
            }
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Referrer-Policy", "no-referrer");
            response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");
            response.setHeader("Cache-Control", "no-store, max-age=0");
            response.setHeader("Pragma", "no-cache");
            if (request.isSecure()) response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        chain.doFilter(request, response);
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;
        private ServletInputStream inputStream;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override public int getContentLength() { return body.length; }
        @Override public long getContentLengthLong() { return body.length; }

        @Override
        public ServletInputStream getInputStream() {
            if (inputStream == null) {
                ByteArrayInputStream content = new ByteArrayInputStream(body);
                inputStream = new ServletInputStream() {
                    @Override public int read() { return content.read(); }
                    @Override public boolean isFinished() { return content.available() == 0; }
                    @Override public boolean isReady() { return true; }
                    @Override public void setReadListener(ReadListener listener) {
                        if (listener == null) throw new IllegalArgumentException("ReadListener is required.");
                        try {
                            listener.onDataAvailable();
                            if (isFinished()) listener.onAllDataRead();
                        } catch (IOException ex) {
                            listener.onError(ex);
                        }
                    }
                };
            }
            return inputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding();
            return new BufferedReader(encoding == null
                    ? new InputStreamReader(getInputStream(), StandardCharsets.ISO_8859_1)
                    : new InputStreamReader(getInputStream(), encoding));
        }
    }
}
