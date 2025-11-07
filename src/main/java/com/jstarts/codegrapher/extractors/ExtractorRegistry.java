package com.jstarts.codegrapher.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExtractorRegistry {

    private final Map<String, List<CodeEntityExtractor>> registry = new ConcurrentHashMap<>();

    public void register(String nodeType, CodeEntityExtractor extractor) {
        registry.computeIfAbsent(nodeType, k -> new ArrayList<>()).add(extractor);
    }

    public List<CodeEntityExtractor> getExtractors(String nodeType) {
        return registry.getOrDefault(nodeType, List.of());
    }
}
