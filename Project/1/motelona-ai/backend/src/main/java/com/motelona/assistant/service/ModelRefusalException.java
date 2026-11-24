package com.motelona.assistant.service;

/** A safety refusal is final for the turn and must not be routed to another provider. */
public class ModelRefusalException extends RuntimeException {
    private final String provider;

    public ModelRefusalException(String provider) {
        super("The model provider refused this request.");
        this.provider = provider;
    }

    public String provider() { return provider; }
}
