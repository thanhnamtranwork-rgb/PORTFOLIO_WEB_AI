package com.motelona.assistant.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectFileSearchServiceTest {
    @TempDir Path projectRoot;

    @Test void skipsGeneratedBuildDirectoriesWhileSearchingProjectFiles() throws Exception {
        Path source = projectRoot.resolve("Project/1/motelona-ai/backend/src/main/java/Source.java");
        Path generated = projectRoot.resolve("Project/1/motelona-ai/backend/target/classes/Generated.java");
        Files.createDirectories(source.getParent());
        Files.createDirectories(generated.getParent());
        Files.writeString(source, "includedMarker");
        Files.writeString(generated, "excludedMarker");

        var localSearch = mock(LocalSearchService.class);
        when(localSearch.searchDocuments(anyString(), anyMap())).thenAnswer(invocation -> {
            var corpus = invocation.<java.util.Map<String, String>>getArgument(1);
            return corpus.entrySet().stream().filter(entry -> entry.getValue().contains("includedMarker"))
                    .map(entry -> new LocalSearchService.SourceMatch(entry.getKey(), new LocalSearchService.Match("includedMarker", 100)))
                    .toList();
        });
        var fileSearch = new ProjectFileSearchService(localSearch, projectRoot.toString());

        List<String> results = fileSearch.search("marker");

        assertTrue(results.stream().anyMatch(result -> result.contains("includedMarker")));
        assertFalse(results.stream().anyMatch(result -> result.contains("excludedMarker")));
    }

    @Test void explicitlyNamedReadmeIsPrioritizedOverAnExactTestPromptMatch() throws Exception {
        Path readme = projectRoot.resolve("README.md");
        Path source = projectRoot.resolve("Project/1/motelona-ai/backend/src/main/java/TestQuestion.java");
        Files.createDirectories(source.getParent());
        Files.writeString(readme, "Model Gemini mặc định là gemini-3.8-flash.");
        Files.writeString(source, "Theo README của dự án, model Gemini mặc định là gì?");

        var localSearch = new LocalSearchService();
        var fileSearch = new ProjectFileSearchService(localSearch, projectRoot.toString());

        List<String> results = fileSearch.search("Theo README của dự án, model Gemini mặc định là gì?");

        assertFalse(results.isEmpty());
        assertTrue(results.getFirst().startsWith("File: README.md\n"));
        assertTrue(results.getFirst().contains("gemini-3.8-flash"));
    }
}
