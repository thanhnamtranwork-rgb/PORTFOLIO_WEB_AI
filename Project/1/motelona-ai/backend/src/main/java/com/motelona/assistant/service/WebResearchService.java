package com.motelona.assistant.service;

import com.motelona.assistant.search.SearchProvider;
import com.motelona.assistant.search.SearchProvider.SearchResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Optional public web retrieval and safe page reading; the chat model evaluates returned evidence. */
@Service
public class WebResearchService {
    private static final Logger log = LoggerFactory.getLogger(WebResearchService.class);
    private static final int MAX_PAGE_BYTES = 1_000_000;
    private static final int MAX_PAGE_TEXT = 3200;
    private final SearchProvider searchProvider;
    private final HttpClient pageClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public WebResearchService(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    public boolean isInternetSearchEnabled() { return searchProvider.isEnabled(); }
    public String providerDescription() { return searchProvider.isEnabled() ? "Public web search and source reading" : "Web search unavailable"; }

    public WebResearch research(String query, String language) {
        if (!searchProvider.isEnabled()) return new WebResearch("", List.of(), "unavailable");
        try {
            List<SearchResult> found = searchProvider.search(query);
            List<SearchResult> enriched = new ArrayList<>();
            for (SearchResult result : found.stream().limit(5).toList()) {
                String pageText = readPublicPage(result.url());
                String evidence = pageText.isBlank() ? result.snippet() : pageText;
                enriched.add(new SearchResult(result.title(), limit(evidence, MAX_PAGE_TEXT), result.url()));
            }
            return new WebResearch("", enriched, "public-search");
        } catch (Exception ex) {
            log.warn("Public web research unavailable; exceptionType={}", ex.getClass().getSimpleName());
            return new WebResearch("Online search could not retrieve readable sources for this turn.", List.of(), "unavailable");
        }
    }

    private String readPublicPage(String value) {
        URI uri;
        try { uri = URI.create(value); }
        catch (Exception ex) { return ""; }
        if (!isPublicHttpUrl(uri)) return "";
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(7))
                    .header("Accept", "text/html,application/xhtml+xml,text/plain;q=0.8")
                    .header("Accept-Language", "vi,en-US;q=0.8,en;q=0.6")
                    .header("User-Agent", "MoteLonaPortfolioAssistant/1.0 (public source reader)")
                    .GET().build();
            HttpResponse<InputStream> response = pageClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || !isPublicHttpUrl(response.uri())) return "";
            String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
            if (!contentType.isBlank() && !contentType.contains("text/html") && !contentType.contains("application/xhtml+xml")
                    && !contentType.contains("text/plain")) return "";
            byte[] bytes;
            try (InputStream input = response.body()) { bytes = input.readNBytes(MAX_PAGE_BYTES + 1); }
            if (bytes.length > MAX_PAGE_BYTES) return "";
            String source = new String(bytes, StandardCharsets.UTF_8);
            String text = contentType.contains("text/plain") ? source : extractVisibleText(source);
            return limit(text.replaceAll("\\s+", " ").trim(), MAX_PAGE_TEXT);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private boolean isPublicHttpUrl(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return false;
        String normalized = host.toLowerCase(Locale.ROOT);
        if (normalized.equals("localhost") || normalized.endsWith(".localhost") || normalized.endsWith(".local")
                || normalized.equals("metadata.google.internal")) return false;
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress()) return false;
            }
            return true;
        } catch (Exception ex) { return false; }
    }

    private String extractVisibleText(String html) {
        StringBuilder out = new StringBuilder();
        try {
            new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
                private int hiddenDepth;
                private static final List<String> HIDDEN = List.of("script", "style", "head", "noscript", "svg", "template", "nav", "footer", "form");
                @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrs, int pos) {
                    String name = tag.toString().toLowerCase(Locale.ROOT);
                    if (HIDDEN.contains(name)) hiddenDepth++;
                    else if (hiddenDepth == 0 && List.of("p", "div", "br", "li", "h1", "h2", "h3", "tr").contains(name)) out.append(' ');
                }
                @Override public void handleEndTag(HTML.Tag tag, int pos) {
                    String name = tag.toString().toLowerCase(Locale.ROOT);
                    if (HIDDEN.contains(name) && hiddenDepth > 0) hiddenDepth--;
                    else if (hiddenDepth == 0 && List.of("p", "div", "li", "h1", "h2", "h3", "tr").contains(name)) out.append(' ');
                }
                @Override public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attrs, int pos) {
                    if (hiddenDepth == 0 && "br".equalsIgnoreCase(tag.toString())) out.append(' ');
                }
                @Override public void handleText(char[] data, int pos) { if (hiddenDepth == 0) out.append(data).append(' '); }
            }, true);
        } catch (Exception ignored) { return ""; }
        return out.toString();
    }

    private String limit(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record WebResearch(String summary, List<SearchResult> sources, String provider) {
        public WebResearch { sources = sources == null ? List.of() : List.copyOf(sources); }
    }
}
