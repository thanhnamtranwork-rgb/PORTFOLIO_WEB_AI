package com.motelona.assistant.database;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MySqlSchemaContractTest {
    @Test
    void schemaContainsPortfolioAndMotelonaTablesUsingMysqlSyntax() throws IOException {
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"), StandardCharsets.UTF_8);
        assertTrue(schema.contains("ENGINE=InnoDB"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS conversation_messages"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS user_memories"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS ai_api_daily_usage"));
        assertTrue(schema.contains("session_id VARCHAR(100) NOT NULL"));
        assertTrue(schema.contains("idx_user_memories_session_id"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS Profile"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS AIKnowledgeItem"));
        assertTrue(schema.contains("MajorVi VARCHAR(150) NULL"));
        assertTrue(schema.contains("DegreeVi VARCHAR(100) NULL"));
        assertTrue(schema.contains("DescriptionVi LONGTEXT NULL"));
        assertTrue(schema.contains("CategoryNameVi VARCHAR(100) NULL"));
        assertTrue(schema.contains("SkillNameVi VARCHAR(100) NULL"));
        assertTrue(schema.contains("TitleVi VARCHAR(200) NULL"));
        assertTrue(schema.contains("ShortDescriptionVi VARCHAR(500) NULL"));
        assertTrue(schema.contains("FullDescriptionVi LONGTEXT NULL"));
        assertTrue(!schema.contains("AUTOINCREMENT"));
        assertTrue(!schema.contains("dbo."));
    }
}
