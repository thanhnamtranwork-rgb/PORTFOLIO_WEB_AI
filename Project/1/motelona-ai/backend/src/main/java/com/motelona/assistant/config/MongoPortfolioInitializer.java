package com.motelona.assistant.config;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/** Creates MongoDB indexes and inserts starter Portfolio records without replacing user-edited data. */
@Component
public class MongoPortfolioInitializer implements ApplicationRunner {
    private static final String PROFILE = "portfolio_profile";
    private static final String EDUCATION = "portfolio_education";
    private static final String EXPERIENCE = "portfolio_experience";
    private static final String SKILLS = "portfolio_skill_categories";
    private static final String PROJECTS = "portfolio_projects";
    private static final String KNOWLEDGE = "ai_knowledge_items";
    private final MongoTemplate mongo;
    private final ObjectMapper mapper;

    public MongoPortfolioInitializer(MongoTemplate mongo, ObjectMapper mapper) {
        this.mongo = mongo;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createIndexes();
        if (!mongo.exists(Query.query(Criteria.where("_id").is("main")), PROFILE)) {
            Document profile = toDocument((Map<?, ?>) seed().get("profile"));
            profile.put("_id", "main");
            mongo.insert(profile, PROFILE);
        }
        seedCollection(EDUCATION, "education");
        seedCollection(EXPERIENCE, "experience");
        seedCollection(SKILLS, "skillCategories");
        seedCollection(PROJECTS, "projects");
        seedCollection(KNOWLEDGE, "knowledge");
    }

    private void createIndexes() {
        mongo.indexOps(EDUCATION).createIndex(new Index().on("isVisible", Sort.Direction.ASC).on("displayOrder", Sort.Direction.ASC));
        mongo.indexOps(EXPERIENCE).createIndex(new Index().on("isVisible", Sort.Direction.ASC).on("displayOrder", Sort.Direction.ASC));
        mongo.indexOps(SKILLS).createIndex(new Index().on("displayOrder", Sort.Direction.ASC));
        mongo.indexOps(PROJECTS).createIndex(new Index().on("slug", Sort.Direction.ASC).unique());
        mongo.indexOps(PROJECTS).createIndex(new Index().on("isPublished", Sort.Direction.ASC).on("isFeatured", Sort.Direction.DESC).on("displayOrder", Sort.Direction.ASC));
        mongo.indexOps(KNOWLEDGE).createIndex(new Index().on("isActive", Sort.Direction.ASC).on("updatedAt", Sort.Direction.DESC));
        mongo.indexOps("contact_messages").createIndex(new Index().on("messageStatus", Sort.Direction.ASC).on("receivedAt", Sort.Direction.DESC));
        mongo.indexOps("conversation_sessions").createIndex(new Index().on("updatedAt", Sort.Direction.DESC));
        mongo.indexOps("conversation_messages").createIndex(new Index().on("sessionId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.ASC).on("_id", Sort.Direction.ASC));
        mongo.indexOps("user_memories").createIndex(new Index().on("sessionId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
    }

    private void seedCollection(String collection, String key) throws Exception {
        if (mongo.getCollection(collection).countDocuments() > 0) return;
        Object value = seed().get(key);
        if (!(value instanceof List<?> rows)) return;
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> raw)) continue;
            Document document = toDocument(raw);
            if (SKILLS.equals(collection) && document.get("skills") instanceof List<?> skills) {
                String categoryId = String.valueOf(document.get("_id"));
                for (Object item : skills) if (item instanceof Document skill) {
                    skill.putIfAbsent("_id", categoryId + "-" + slug(String.valueOf(skill.get("skillName"))));
                }
            }
            mongo.insert(document, collection);
        }
    }

    private Document toDocument(Map<?, ?> source) {
        Document result = new Document();
        source.forEach((key, value) -> { if (key instanceof String name) result.put(name, mongoValue(value)); });
        return result;
    }

    private Object mongoValue(Object value) {
        if (value instanceof Map<?, ?> map) return toDocument(map);
        if (value instanceof List<?> list) return list.stream().map(this::mongoValue).toList();
        return value;
    }

    private String slug(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private Map<String, Object> seed() throws Exception {
        try (InputStream input = new ClassPathResource("portfolio-seed.json").getInputStream()) {
            return mapper.readValue(input, new TypeReference<>() {});
        }
    }
}
