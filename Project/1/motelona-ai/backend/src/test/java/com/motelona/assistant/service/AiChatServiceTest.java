package com.motelona.assistant.service;

import com.motelona.assistant.dto.ChatRequest;
import com.motelona.assistant.memory.MemoryService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiChatServiceTest {
    @Test void greetingGetsAnImmediateReplyWithoutCallingTheModelOrRetrieval() {
        var modelRouter = mock(ModelProviderRouter.class);
        var localFallback = mock(com.motelona.assistant.ai.LocalAiEngine.class);
        var portfolio = mock(PortfolioService.class);
        var localSearch = mock(LocalSearchService.class);
        var files = mock(ProjectFileSearchService.class);
        var webResearch = mock(WebResearchService.class);
        var service = new AiChatService(modelRouter, new ModelPromptBuilder(), new ScopeGuard(), localFallback,
                mock(MemoryService.class),
                mock(ActionRouter.class), localSearch, portfolio, files, webResearch);

        var answer = service.chat(new ChatRequest("test-session", "chào", "vi", "page context",
                List.of(), false, false, false));

        assertEquals("Chào bạn! Mình đây — bạn muốn hỏi gì?", answer.message());
        assertEquals("local", answer.source());
        verifyNoInteractions(modelRouter, localFallback, portfolio, localSearch, files, webResearch);
    }

    @Test void unrelatedQuestionIsStoppedBeforeAnyHostedModelCall() {
        var modelRouter = mock(ModelProviderRouter.class);
        when(modelRouter.isConfigured()).thenReturn(true);
        var localSearch = mock(LocalSearchService.class);
        when(localSearch.search(anyString(), anyString())).thenReturn(List.of());
        var files = mock(ProjectFileSearchService.class);
        when(files.search(anyString())).thenReturn(List.of());
        var service = new AiChatService(modelRouter, new ModelPromptBuilder(), new ScopeGuard(),
                mock(com.motelona.assistant.ai.LocalAiEngine.class),
                mock(MemoryService.class), mock(ActionRouter.class), localSearch,
                mock(PortfolioService.class), files, mock(WebResearchService.class));

        var answer = service.chat(new ChatRequest("test-session", "Viết cho tôi một bài thơ về mèo",
                "vi", "", List.of(), false, false, false));

        assertEquals("scope-guard", answer.source());
        assertFalse(answer.aiPowered());
        assertTrue(answer.message().contains("Portfolio"));
        verifyNoInteractions(modelRouter, localSearch, files);
    }

    @Test void explicitWebSearchAllowsAnOtherwiseOutOfScopeQuestion() {
        List<ModelPrompt> prompts = new ArrayList<>();
        var modelRouter = mock(ModelProviderRouter.class);
        when(modelRouter.isConfigured()).thenReturn(true);
        when(modelRouter.answer(any())).thenAnswer(invocation -> {
            prompts.add(invocation.getArgument(0));
            return new ModelProviderRouter.ProviderAnswer("gemini", "test", "Answer with web sources");
        });
        var webResearch = mock(WebResearchService.class);
        when(webResearch.research(anyString(), anyString())).thenReturn(
                new WebResearchService.WebResearch("A public source", List.of(), "enabled"));
        var localSearch = mock(LocalSearchService.class);
        when(localSearch.search(anyString(), anyString())).thenReturn(List.of());
        var files = mock(ProjectFileSearchService.class);
        when(files.search(anyString())).thenReturn(List.of());
        var service = new AiChatService(modelRouter, new ModelPromptBuilder(), new ScopeGuard(),
                mock(com.motelona.assistant.ai.LocalAiEngine.class),
                mock(MemoryService.class), mock(ActionRouter.class), localSearch,
                mock(PortfolioService.class), files, webResearch);

        var answer = service.chat(new ChatRequest("test-session", "Thời tiết hôm nay thế nào?",
                "vi", "", List.of(), false, false, true));

        assertEquals("Answer with web sources", answer.message());
        assertEquals(1, prompts.size());
        verify(webResearch).research("Thời tiết hôm nay thế nào?", "vi");
    }

    @Test void projectFileQuestionDoesNotTriggerProjectInventoryGuidance() {
        List<String> guidance = new ArrayList<>();
        var service = serviceCapturingGuidance(guidance);

        service.chat(new ChatRequest("test-session", "Theo README của dự án, model Gemini mặc định là gì?",
                "vi", "", List.of(), false, false, false));

        assertEquals(1, guidance.size());
        assertFalse(guidance.getFirst().contains("complete published project inventory"));
    }

    @Test void projectInventoryQuestionGetsInventoryGuidance() {
        List<String> guidance = new ArrayList<>();
        var service = serviceCapturingGuidance(guidance);

        service.chat(new ChatRequest("test-session", "Trang này có những dự án nào?",
                "vi", "", List.of(), false, false, false));

        assertEquals(1, guidance.size());
        assertTrue(guidance.getFirst().contains("complete published project inventory"));
    }

    private AiChatService serviceCapturingGuidance(List<String> captured) {
        var modelRouter = mock(ModelProviderRouter.class);
        when(modelRouter.isConfigured()).thenReturn(true);
        when(modelRouter.answer(any())).thenAnswer(invocation -> {
            ModelPrompt prompt = invocation.getArgument(0);
            captured.add(prompt.userPrompt());
            return new ModelProviderRouter.ProviderAnswer("gemini", "test", "Test answer");
        });
        var portfolio = mock(PortfolioService.class);
        var localSearch = mock(LocalSearchService.class);
        when(localSearch.search(anyString(), anyString())).thenReturn(List.of());
        var files = mock(ProjectFileSearchService.class);
        when(files.search(anyString())).thenReturn(List.of());
        return new AiChatService(modelRouter, new ModelPromptBuilder(), new ScopeGuard(),
                mock(com.motelona.assistant.ai.LocalAiEngine.class), mock(MemoryService.class),
                mock(ActionRouter.class), localSearch, portfolio, files, mock(WebResearchService.class));
    }
}
