package com.jstarts.codegrapher.extractors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExtractorRegistry {

    private final Map<String, CodeEntityExtractor> registry = new ConcurrentHashMap<>();

    public void register(String key, CodeEntityExtractor extractor) {
        registry.put(key, extractor);
    }
    public Optional<CodeEntityExtractor> getExtractor(String key) {
        return Optional.ofNullable(registry.get(key));
    }
}
