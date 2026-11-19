package com.motelona.assistant.search;

import java.util.List;

public interface SearchProvider {
    boolean isEnabled();
    List<SearchResult> search(String query);
    record SearchResult(String title, String snippet, String url) { }
}
