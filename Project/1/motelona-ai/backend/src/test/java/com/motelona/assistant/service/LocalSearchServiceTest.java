package com.motelona.assistant.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LocalSearchServiceTest {
    private final LocalSearchService service = new LocalSearchService();

    @Test
    void returnsOnlyRelevantSnippetsAndRanksMatches() {
        List<LocalSearchService.Match> matches = service.search(
                "kinh doanh CNTT",
                "Nam học kinh doanh và công nghệ thông tin.\nMoteLona là trợ lý AI.\nKinh doanh là một ngành học.");

        assertFalse(matches.isEmpty());
        assertTrue(matches.get(0).score() >= matches.get(matches.size() - 1).score());
        assertTrue(matches.stream().anyMatch(m -> m.snippet().contains("kinh doanh")));
    }

    @Test
    void ignoresBlankQueriesAndContext() {
        assertTrue(service.search("  ", "some page text").isEmpty());
        assertTrue(service.search("portfolio", "  ").isEmpty());
    }

    @Test
    void normalizesVietnameseDiacriticsForSearch() {
        List<LocalSearchService.Match> matches = service.search("cong nghe", "Công nghệ thông tin là lĩnh vực học tập.");
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).snippet().contains("Công nghệ"));
    }
}
