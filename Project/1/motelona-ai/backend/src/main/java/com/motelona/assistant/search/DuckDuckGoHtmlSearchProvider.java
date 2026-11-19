package com.motelona.assistant.search;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.springframework.stereotype.Component;

/** Search adapter for DuckDuckGo's public, non-JavaScript HTML results page. No key or AI API is used. */
@Component
public class DuckDuckGoHtmlSearchProvider implements SearchProvider {
    private static final String ENDPOINT = "https://html.duckduckgo.com/html/?q=";
    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_RESPONSE_BYTES = 1_500_000;
    private static final int MAX_RESULTS = 8;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<SearchResult> search(String query) {
        String cleanQuery = query == null ? "" : query.trim();
        if (cleanQuery.isBlank()) throw new IllegalArgumentException("Search query is required.");
        if (cleanQuery.length() > MAX_QUERY_LENGTH) throw new IllegalArgumentException("Search query must be 200 characters or fewer.");

        URI uri = URI.create(ENDPOINT + URLEncoder.encode(cleanQuery, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "vi,en-US;q=0.8,en;q=0.6")
                .header("User-Agent", "MoteLonaPortfolioAssistant/1.0 (public HTML search results)")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            byte[] body;
            try (InputStream stream = response.body()) {
                body = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300 || body.length > MAX_RESPONSE_BYTES) {
                throw new SearchUnavailableException("Internet search returned an unexpected response.");
            }
            List<SearchResult> results = parseSearchResults(new String(body, StandardCharsets.UTF_8));
            if (results.isEmpty()) throw new SearchUnavailableException("Internet search returned no readable results. Try a shorter query.");
            return results;
        } catch (SearchUnavailableException | IllegalArgumentException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SearchUnavailableException("Internet search was interrupted. Try again.");
        } catch (Exception ex) {
            throw new SearchUnavailableException("Internet search is temporarily unavailable. Check the server's Internet connection and try again.");
        }
    }

    static List<SearchResult> parseSearchResults(String html) {
        if (html == null || html.isBlank()) return List.of();
        List<ResultBuilder> parsed = new ArrayList<>();
        try {
            new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
                private ResultBuilder current;
                private int capture;

                @Override
                public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                    String classes = attribute(attributes, HTML.Attribute.CLASS).toLowerCase(Locale.ROOT);
                    if (tag == HTML.Tag.A && hasClass(classes, "result__a")) {
                        current = new ResultBuilder();
                        current.url = safeResultUrl(attribute(attributes, HTML.Attribute.HREF));
                        parsed.add(current);
                        capture = 1;
                    } else if (current != null && (tag == HTML.Tag.A || tag == HTML.Tag.DIV)
                            && hasClass(classes, "result__snippet")) {
                        capture = 2;
                    }
                }

                @Override
                public void handleText(char[] data, int position) {
                    if (current == null || capture == 0) return;
                    if (capture == 1) current.title.append(data);
                    else current.snippet.append(data);
                }

                @Override
                public void handleEndTag(HTML.Tag tag, int position) {
                    if (capture == 1 && tag == HTML.Tag.A) capture = 0;
                    else if (capture == 2 && (tag == HTML.Tag.A || tag == HTML.Tag.DIV)) capture = 0;
                }
            }, true);
        } catch (Exception ignored) {
            return List.of();
        }

        List<SearchResult> results = new ArrayList<>();
        for (ResultBuilder item : parsed) {
            String title = cleanText(item.title.toString());
            String snippet = cleanText(item.snippet.toString());
            if (title.isBlank() || item.url == null) continue;
            results.add(new SearchResult(title, snippet, item.url));
            if (results.size() == MAX_RESULTS) break;
        }
        return List.copyOf(results);
    }

    private static String attribute(MutableAttributeSet attributes, HTML.Attribute key) {
        Object value = attributes.getAttribute(key);
        return value == null ? "" : value.toString();
    }

    private static boolean hasClass(String classes, String target) {
        return List.of(classes.split("\\s+")).contains(target);
    }

    private static String safeResultUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        try {
            String value = rawUrl.trim();
            if (value.startsWith("//")) value = "https:" + value;
            URI candidate = URI.create(value);
            String query = candidate.getRawQuery();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2 && "uddg".equals(parts[0])) {
                        value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
            URI resolved = URI.create(value);
            String scheme = resolved.getScheme();
            if (resolved.getHost() == null || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) return null;
            return resolved.toASCIIString();
        } catch (Exception ex) {
            return null;
        }
    }

    private static String cleanText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static final class ResultBuilder {
        private final StringBuilder title = new StringBuilder();
        private final StringBuilder snippet = new StringBuilder();
        private String url;
    }
}
