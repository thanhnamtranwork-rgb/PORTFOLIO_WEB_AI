package com.motelona.assistant.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

class PortfolioServiceTest {
    @Test
    void rejectsInvalidContactEmailBeforeDatabaseWrite() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        PortfolioService service = new PortfolioService(mongo);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveContact("Nguyen Van A", "not-an-email", "Subject", "A valid message body"));
        verifyNoInteractions(mongo);
    }

    @Test
    void rejectsOverlongContactMessageBeforeDatabaseWrite() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        PortfolioService service = new PortfolioService(mongo);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveContact("Nguyen Van A", "a@example.com", "Subject", "x".repeat(5001)));
        verifyNoInteractions(mongo);
    }
}
