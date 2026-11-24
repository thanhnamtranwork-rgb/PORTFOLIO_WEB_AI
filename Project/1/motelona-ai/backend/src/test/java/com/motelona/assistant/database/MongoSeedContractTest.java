package com.motelona.assistant.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MongoSeedContractTest {
    @Test
    void seedIncludesBilingualPortfolioAndEmbeddedProjectTechnologies() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> seed;
        try (InputStream input = getClass().getResourceAsStream("/portfolio-seed.json")) {
            seed = mapper.readValue(input, new TypeReference<>() {});
        }

        assertTrue(seed.containsKey("profile"));
        assertEquals(1, ((List<?>) seed.get("education")).size());
        assertEquals(3, ((List<?>) seed.get("skillCategories")).size());
        assertEquals(1, ((List<?>) seed.get("projects")).size());
        Map<?, ?> project = (Map<?, ?>) ((List<?>) seed.get("projects")).getFirst();
        assertEquals("motelona-ai-website-assistant", project.get("slug"));
        assertTrue(((List<?>) project.get("technologies")).stream().anyMatch(technology ->
                "MongoDB".equals(((Map<?, ?>) technology).get("technologyName"))));
        assertTrue(project.containsKey("fullDescriptionVi"));
    }
}
