package com.motelona.assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${motelona.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500,http://localhost:8000,http://127.0.0.1:8000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(value -> !value.isBlank()).toArray(String[]::new);
        if (origins.length == 0 || java.util.Arrays.stream(origins).anyMatch("*"::equals)) {
            throw new IllegalStateException("MOTELONA_CORS_ORIGINS must contain explicit origins; wildcard CORS is disabled.");
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept")
                .maxAge(3600);
    }
}
