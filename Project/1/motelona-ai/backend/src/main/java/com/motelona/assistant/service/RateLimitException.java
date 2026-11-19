package com.motelona.assistant.service;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) { super(message); }
}
