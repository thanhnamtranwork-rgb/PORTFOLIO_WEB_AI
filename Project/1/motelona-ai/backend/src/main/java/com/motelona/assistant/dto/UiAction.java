package com.motelona.assistant.dto;

public record UiAction(String type, String target, String term) {
    public static UiAction navigate(String target) { return navigate(target, null); }
    public static UiAction navigate(String target, String term) { return new UiAction("navigate", target, term); }
    public static UiAction explain(String term) { return new UiAction("explain", null, term); }
}
