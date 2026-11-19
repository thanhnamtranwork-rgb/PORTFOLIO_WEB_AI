package com.motelona.assistant.service;

import com.motelona.assistant.memory.MemoryService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemoryServiceTest {
    @Test
    void refusesToSavePasswordBeforeDatabaseAccess() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MemoryService service = new MemoryService(jdbc);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "Mật khẩu tài khoản là abc123"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void refusesToSaveApiKeysBeforeDatabaseAccess() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MemoryService service = new MemoryService(jdbc);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "API key của dự án là xyz"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void refusesMemoryThatIsTooShortBeforeDatabaseAccess() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MemoryService service = new MemoryService(jdbc);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "AI"));
        verifyNoInteractions(jdbc);
    }
}
