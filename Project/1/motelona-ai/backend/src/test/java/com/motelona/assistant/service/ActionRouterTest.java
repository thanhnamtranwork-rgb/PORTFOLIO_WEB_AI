package com.motelona.assistant.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionRouterTest {
    private final ActionRouter router = new ActionRouter();
    @Test void routesAnExplicitVietnameseNavigationRequest() {
        var action = router.routeExplicitNavigation("Cuộn đến phần dự án");
        assertNotNull(action); assertEquals("navigate", action.type()); assertEquals("projects", action.target());
    }
    @Test void routesAnExplicitEnglishNavigationRequest() {
        var action = router.routeExplicitNavigation("Take me to the skills section");
        assertNotNull(action); assertEquals("skills", action.target());
    }
    @Test void doesNotTreatOrdinaryQuestionAsNavigation() {
        assertNull(router.routeExplicitNavigation("What projects are on this page?"));
    }
    @Test void routesVietnameseNavigationToMotelonaArchitecture() {
        var action = router.routeExplicitNavigation("Cuộn đến phần kiến trúc");
        assertNotNull(action); assertEquals("architecture", action.target());
        assertEquals("kiến trúc", router.navigationLabel(action.target(), "vi"));
    }
    @Test void routesEnglishNavigationToMotelonaGlossary() {
        var action = router.routeExplicitNavigation("Take me to the NLP basics section");
        assertNotNull(action); assertEquals("knowledge", action.target());
    }
    @Test void routesVietnameseNavigationToMotelonaRoadmap() {
        var action = router.routeExplicitNavigation("Đưa tôi đến lộ trình");
        assertNotNull(action); assertEquals("roadmap", action.target());
    }

    @Test void prefersSpecificMotelonaDestinationOverBroadProjectKeyword() {
        var action = router.routeExplicitNavigation("Take me to the project architecture");
        assertNotNull(action); assertEquals("architecture", action.target());
    }

    @Test void rejectsUnknownSectionByNotProducingAction() {
        assertNull(router.routeExplicitNavigation("Navigate to the administration console"));
    }

    @Test void routesToTheRelevantParagraphAndCarriesThePhraseToHighlight() {
        var action = router.routeExplicitNavigation("Take me to the paragraph about MySQL schema", "SECTION #database: MySQL schema and database setup");
        assertNotNull(action);
        assertEquals("database", action.target());
        assertEquals("MySQL schema", action.term());
    }

    @Test void routesByPageContextWhenTheSectionNameIsNotInTheRequest() {
        var action = router.routeExplicitNavigation("Take me to the part about consent and saved memories",
                "SECTION #features: Visitors can explain terms.\n\nSECTION #quality: Consent-first storage keeps history and memories off until enabled.");
        assertNotNull(action);
        assertEquals("quality", action.target());
        assertEquals("consent and saved memories", action.term());
    }
}
