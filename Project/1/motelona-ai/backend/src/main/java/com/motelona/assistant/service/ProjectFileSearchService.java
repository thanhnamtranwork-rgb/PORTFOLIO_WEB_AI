package com.motelona.assistant.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Searches an allow-listed set of project source and documentation files; secrets and build output are excluded. */
@Service
public class ProjectFileSearchService {
    private static final int MAX_FILES = 1000;
    private static final long MAX_FILE_BYTES = 300_000;
    private static final long MAX_CORPUS_BYTES = 10_000_000;
    private static final long CACHE_MILLIS = 300_000;
    private static final Set<String> EXTENSIONS = Set.of(".md", ".txt", ".java", ".js", ".css", ".html", ".xml",
            ".yml", ".yaml", ".properties", ".json", ".ps1", ".bat", ".cmd", ".py", ".toml", ".ini", ".csv", ".sh", ".conf");

    private final Path projectRoot;
    private final LocalSearchService localSearch;
    private volatile List<FileDocument> cachedDocuments = List.of();
    private volatile long cachedAt;

    public ProjectFileSearchService(LocalSearchService localSearch,
                                    @Value("${motelona.project-root:../../../../}") String projectRoot) {
        this.localSearch = localSearch;
        this.projectRoot = Paths.get(projectRoot == null || projectRoot.isBlank() ? "../../../../" : projectRoot)
                .toAbsolutePath().normalize();
    }

    public List<String> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        Map<String, String> corpus = new LinkedHashMap<>();
        for (FileDocument document : documents()) corpus.put(document.relativePath(), document.content());
        List<ScoredPassage> matches = localSearch.searchDocuments(query, corpus).stream()
                .map(row -> new ScoredPassage(row.match().score(), "File: " + row.source() + "\n" + row.match().snippet()))
                .toList();
        Set<String> seen = new HashSet<>();
        return matches.stream()
                .sorted(Comparator.comparingInt(ScoredPassage::score).reversed())
                .filter(item -> seen.add(item.passage().toLowerCase(Locale.ROOT)))
                .limit(6)
                .map(ScoredPassage::passage)
                .toList();
    }

    private List<FileDocument> documents() {
        long now = System.currentTimeMillis();
        List<FileDocument> current = cachedDocuments;
        if (!current.isEmpty() && now - cachedAt < CACHE_MILLIS) return current;
        synchronized (this) {
            if (!cachedDocuments.isEmpty() && now - cachedAt < CACHE_MILLIS) return cachedDocuments;
            cachedDocuments = loadDocuments();
            cachedAt = now;
            return cachedDocuments;
        }
    }

    private List<FileDocument> loadDocuments() {
        if (!Files.isDirectory(projectRoot)) return List.of();
        List<Path> roots = List.of(
                projectRoot.resolve("README.md"), projectRoot.resolve("index.html"),
                projectRoot.resolve("Project/1/motelona.html"), projectRoot.resolve("docs"),
                projectRoot.resolve("knowledge-base"), projectRoot.resolve("Project/1/motelona-ai"), projectRoot.resolve("tools"),
                projectRoot.resolve(".github"), projectRoot.resolve(".vscode"),
                projectRoot.resolve("CHAY_MOTELONA.bat"), projectRoot.resolve("CHAY_MOTELONA.ps1"),
                projectRoot.resolve("CHAY_MOTELONA_AI_SETUP.bat"));
        List<FileDocument> documents = new ArrayList<>();
        long totalBytes = 0;
        for (Path scanRoot : roots) {
            if (documents.size() >= MAX_FILES || totalBytes >= MAX_CORPUS_BYTES) break;
            if (Files.isRegularFile(scanRoot)) {
                totalBytes += addDocument(scanRoot, documents, totalBytes);
                continue;
            }
            if (!Files.isDirectory(scanRoot)) continue;
            long[] corpusBytes = {totalBytes};
            try {
                Files.walkFileTree(scanRoot, java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), 10,
                        new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                                return excludedDirectory(directory) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
                                if (documents.size() >= MAX_FILES || corpusBytes[0] >= MAX_CORPUS_BYTES) {
                                    return FileVisitResult.TERMINATE;
                                }
                                corpusBytes[0] += addDocument(path, documents, corpusBytes[0]);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path path, IOException error) {
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException ignored) { }
            totalBytes = corpusBytes[0];
        }
        return List.copyOf(documents);
    }

    private boolean excludedDirectory(Path path) {
        String value = "/" + projectRoot.relativize(path.toAbsolutePath().normalize())
                .toString().replace('\\', '/').toLowerCase() + "/";
        return value.contains("/.git/") || value.contains("/target/") || value.contains("/node_modules/")
                || value.contains("/attachments/") || value.contains("/.attachments/") || value.contains("/dist/")
                || value.contains("/build/") || value.contains("/.venv/") || value.contains("/venv/")
                || value.contains("/.idea/") || value.contains("/.gradle/") || value.contains("/coverage/");
    }

    private long addDocument(Path path, List<FileDocument> documents, long totalBytes) {
        if (documents.size() >= MAX_FILES || totalBytes >= MAX_CORPUS_BYTES || excluded(path)) return 0;
        try {
            long size = Files.size(path);
            if (size <= 0 || size > MAX_FILE_BYTES || totalBytes + size > MAX_CORPUS_BYTES) return 0;
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (content.isBlank()) return 0;
            String relative = projectRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
            documents.add(new FileDocument(relative, content));
            return size;
        } catch (IOException | RuntimeException ex) { return 0; }
    }

    private boolean excluded(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        String value = path.toString().replace('\\', '/').toLowerCase();
        if (name.startsWith(".env") || name.contains("secret") || name.contains("credential") || name.contains("password")) return true;
        if (value.contains("/target/") || value.contains("/node_modules/") || value.contains("/.git/")
                || value.contains("/attachments/") || value.contains("/.attachments/") || value.contains("/dist/")
                || value.contains("/build/") || value.contains("/.venv/") || value.contains("/venv/")
                || value.contains("/.idea/") || value.contains("/.gradle/") || value.contains("/coverage/")) return true;
        int dot = name.lastIndexOf('.');
        return dot < 0 || !EXTENSIONS.contains(name.substring(dot));
    }

    private record FileDocument(String relativePath, String content) { }
    private record ScoredPassage(int score, String passage) { }
}
