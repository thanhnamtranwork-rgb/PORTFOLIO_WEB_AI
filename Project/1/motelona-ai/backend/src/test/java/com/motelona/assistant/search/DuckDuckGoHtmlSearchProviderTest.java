package com.motelona.assistant.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DuckDuckGoHtmlSearchProviderTest {
    @Test
    void parsesTitlesSnippetsAndUnwrapsSearchRedirects() {
        String html = """
                <div class="result">
                  <a class="result__a" href="https://duckduckgo.com/l/?uddg=https%3A%2F%2Fexample.com%2Fguide&amp;rut=test">Java <b>Guide</b></a>
                  <a class="result__snippet">Official documentation <b>for Java</b>.</a>
                </div>
                """;

        List<SearchProvider.SearchResult> results = DuckDuckGoHtmlSearchProvider.parseSearchResults(html);

        assertEquals(1, results.size());
        assertEquals("Java Guide", results.getFirst().title());
        assertEquals("Official documentation for Java.", results.getFirst().snippet());
        assertEquals("https://example.com/guide", results.getFirst().url());
    }

    @Test
    void ignoresUnsafeAndNonSearchLinks() {
        String html = "<a class='result__a' href='javascript:alert(1)'>bad</a>";
        assertTrue(DuckDuckGoHtmlSearchProvider.parseSearchResults(html).isEmpty());
    }
}
