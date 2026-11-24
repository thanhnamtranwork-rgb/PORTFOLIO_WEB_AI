package com.motelona.assistant.memory;

import com.motelona.assistant.dto.MemoryItem;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class MemoryService {
    private static final String COLLECTION = "user_memories";
    private final MongoTemplate mongo;

    public MemoryService(MongoTemplate mongo) { this.mongo = mongo; }

    public MemoryItem saveExplicitly(String sessionId, String content) {
        String owner = validateSession(sessionId);
        String safe = normalize(content);
        if (safe.length() < 4) throw new IllegalArgumentException("Memory text is too short.");
        if (safe.length() > 800) throw new IllegalArgumentException("Memory text must be 800 characters or fewer.");
        String lower = safe.toLowerCase(Locale.ROOT);
        if (lower.matches(".*(password|mật khẩu|api[ _-]?key|secret|access token|private key).*")) {
            throw new IllegalArgumentException("For safety, passwords, API keys, and secrets cannot be saved as long-term memory.");
        }
        ObjectId id = new ObjectId();
        Date createdAt = new Date();
        mongo.insert(new Document("_id", id).append("sessionId", owner).append("content", safe).append("createdAt", createdAt), COLLECTION);
        return new MemoryItem(id.toHexString(), safe, createdAt.toInstant().toString());
    }

    public List<MemoryItem> list(String sessionId) {
        String owner = validateSession(sessionId);
        Query query = Query.query(Criteria.where("sessionId").is(owner))
                .with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(100);
        return mongo.find(query, Document.class, COLLECTION).stream().map(document -> new MemoryItem(
                document.getObjectId("_id").toHexString(), document.getString("content"),
                document.getDate("createdAt").toInstant().toString())).toList();
    }

    public void delete(String sessionId, String id) {
        String owner = validateSession(sessionId);
        if (id == null || !ObjectId.isValid(id)) return;
        mongo.remove(Query.query(Criteria.where("sessionId").is(owner).and("_id").is(new ObjectId(id))), COLLECTION);
    }

    public void deleteAll(String sessionId) {
        mongo.remove(Query.query(Criteria.where("sessionId").is(validateSession(sessionId))), COLLECTION);
    }

    public int forgetMatching(String sessionId, String phrase) {
        String owner = validateSession(sessionId);
        String p = normalize(phrase).toLowerCase(Locale.ROOT);
        if (p.isBlank() || p.matches("(all|everything|tất cả|mọi thứ|toàn bộ)")) {
            long count = mongo.count(Query.query(Criteria.where("sessionId").is(owner)), COLLECTION);
            deleteAll(owner);
            return (int) Math.min(Integer.MAX_VALUE, count);
        }
        List<Document> matches = mongo.find(Query.query(Criteria.where("sessionId").is(owner)), Document.class, COLLECTION)
                .stream().filter(document -> {
                    String content = document.getString("content").toLowerCase(Locale.ROOT);
                    return content.contains(p) || p.contains(content);
                }).toList();
        matches.forEach(document -> delete(owner, document.getObjectId("_id").toHexString()));
        return matches.size();
    }

    public List<String> memoryText(String sessionId, boolean enabled) {
        if (!enabled) return List.of();
        return list(sessionId).stream().limit(12).map(MemoryItem::content).toList();
    }

    private String validateSession(String value) {
        String id = value == null ? "" : value.trim();
        if (!id.matches("[A-Za-z0-9._-]{4,100}")) throw new IllegalArgumentException("Invalid session ID format.");
        return id;
    }
    private String normalize(String text) { return text == null ? "" : text.trim().replaceAll("\\s+", " "); }
}
