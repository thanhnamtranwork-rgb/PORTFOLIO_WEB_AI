package com.motelona.assistant.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class PortfolioServiceTest {
    @Test
    void rejectsInvalidContactEmailBeforeDatabaseWrite() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        PortfolioService service = new PortfolioService(jdbc);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveContact("Nguyen Van A", "not-an-email", "Subject", "A valid message body"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejectsOverlongContactMessageBeforeDatabaseWrite() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        PortfolioService service = new PortfolioService(jdbc);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveContact("Nguyen Van A", "a@example.com", "Subject", "x".repeat(5001)));
        verifyNoInteractions(jdbc);
    }
}
