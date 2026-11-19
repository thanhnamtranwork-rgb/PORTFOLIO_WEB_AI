package com.motelona.assistant.memory;

import com.motelona.assistant.dto.MemoryItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;

@Service
public class MemoryService {
    private final JdbcTemplate jdbc;
    public MemoryService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public MemoryItem saveExplicitly(String sessionId, String content) {
        String owner = validateSession(sessionId);
        String safe = normalize(content);
        if (safe.length() < 4) throw new IllegalArgumentException("Memory text is too short.");
        if (safe.length() > 800) throw new IllegalArgumentException("Memory text must be 800 characters or fewer.");
        String lower = safe.toLowerCase(Locale.ROOT);
        if (lower.matches(".*(password|mật khẩu|api[ _-]?key|secret|access token|private key).*")) {
            throw new IllegalArgumentException("For safety, passwords, API keys, and secrets cannot be saved as long-term memory.");
        }
        jdbc.update("INSERT INTO user_memories(session_id, content) VALUES (?, ?)", owner, safe);
        List<MemoryItem> saved = jdbc.query("SELECT id, content, created_at FROM user_memories WHERE session_id = ? ORDER BY id DESC LIMIT 1",
                (rs, row) -> new MemoryItem(rs.getLong("id"),rs.getString("content"),rs.getString("created_at")), owner);
        return saved.isEmpty() ? null : saved.get(0);
    }

    public List<MemoryItem> list(String sessionId) {
        String owner = validateSession(sessionId);
        return jdbc.query("SELECT id, content, created_at FROM user_memories WHERE session_id = ? ORDER BY id DESC LIMIT 100",
                (rs, row) -> new MemoryItem(rs.getLong("id"), rs.getString("content"), rs.getString("created_at")), owner);
    }

    public void delete(String sessionId, long id) {
        jdbc.update("DELETE FROM user_memories WHERE session_id = ? AND id = ?", validateSession(sessionId), id);
    }

    public void deleteAll(String sessionId) {
        jdbc.update("DELETE FROM user_memories WHERE session_id = ?", validateSession(sessionId));
    }

    public int forgetMatching(String sessionId, String phrase) {
        String owner = validateSession(sessionId);
        String p = normalize(phrase).toLowerCase(Locale.ROOT);
        if (p.isBlank() || p.matches("(all|everything|tất cả|mọi thứ|toàn bộ)")) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_memories WHERE session_id = ?", Integer.class, owner);
            deleteAll(owner);
            return count == null ? 0 : count;
        }
        List<Long> ids = jdbc.query("SELECT id, content FROM user_memories WHERE session_id = ?", (rs, row) -> {
            String content = rs.getString("content").toLowerCase(Locale.ROOT);
            return content.contains(p) || p.contains(content) ? rs.getLong("id") : null;
        }, owner).stream().filter(id -> id != null).toList();
        ids.forEach(id -> delete(owner, id));
        return ids.size();
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
