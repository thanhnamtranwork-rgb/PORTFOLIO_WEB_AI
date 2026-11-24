package com.motelona.assistant.service;

import com.motelona.assistant.memory.MemoryService;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemoryServiceTest {
    @Test
    void refusesToSavePasswordBeforeDatabaseAccess() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        MemoryService service = new MemoryService(mongo);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "Mật khẩu tài khoản là abc123"));
        verifyNoInteractions(mongo);
    }

    @Test
    void refusesToSaveApiKeysBeforeDatabaseAccess() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        MemoryService service = new MemoryService(mongo);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "API key của dự án là xyz"));
        verifyNoInteractions(mongo);
    }

    @Test
    void refusesMemoryThatIsTooShortBeforeDatabaseAccess() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        MemoryService service = new MemoryService(mongo);

        assertThrows(IllegalArgumentException.class, () -> service.saveExplicitly("test-session-123", "AI"));
        verifyNoInteractions(mongo);
    }
}
