package com.motelona.assistant.dto;

public record ConversationItem(long id, String role, String content, String createdAt) { }
