package com.motelona.assistant.dto;

public record ContactMessageRequest(String name, String email, String subject, String message, String honeypot) { }
