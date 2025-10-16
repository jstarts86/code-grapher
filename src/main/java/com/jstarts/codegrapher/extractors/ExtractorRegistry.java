package com.jstarts.codegrapher.extractors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtractorRegistry {
    private static final Map<String, CodeEntityExtractor> registry = new ConcurrentHashMap<>();

    public static void register(String key, CodeEntityExtractor extractor) {
        registry.put(key, extractor);
    }



    
}
