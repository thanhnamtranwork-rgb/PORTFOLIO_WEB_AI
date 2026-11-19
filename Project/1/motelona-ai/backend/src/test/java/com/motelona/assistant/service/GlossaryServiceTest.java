package com.motelona.assistant.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlossaryServiceTest {
    private final GlossaryService glossary = new GlossaryService();
    @Test void findsKnownTermCaseInsensitively() {
        var term = glossary.find("rest api"); assertNotNull(term); assertEquals("REST API",term.term()); assertFalse(term.vi().isBlank());
    }
    @Test void returnsNullForUnknownTerm() { assertNull(glossary.find("unknown xyz term")); }
    @Test void returnsNullForBlankInput() { assertNull(glossary.find("  ")); }
    @Test void findsNewLocalAiTermsWithoutExternalProviderTerms() {
        var term = glossary.find("TF-IDF là gì?"); assertNotNull(term); assertEquals("TF-IDF", term.term());
        assertTrue(glossary.findMentioned("MoteLona uses TF-IDF and Cosine Similarity.").size() >= 2);
    }
}
