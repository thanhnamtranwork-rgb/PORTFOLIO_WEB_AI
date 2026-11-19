package com.motelona.assistant.dto;

import java.time.Instant;

public record ApiError(String error, String message, String timestamp) {
    public static ApiError of(String error, String message) {
        return new ApiError(error, message, Instant.now().toString());
    }
}
