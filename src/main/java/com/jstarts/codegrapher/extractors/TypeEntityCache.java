package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.CodeEntity;

import com.jstarts.codegrapher.core.entities.PythonType;
import com.jstarts.codegrapher.core.entities.SourceLocation;
import com.jstarts.codegrapher.core.entities.TypeEntity;

import java.util.HashMap;
import java.util.Map;

public class TypeEntityCache {

    private final Map<PythonType, TypeEntity> cache = new HashMap<>();
    private final ExtractionContext context;

    public TypeEntityCache(ExtractionContext context) {
        this.context = context;
    }

    public TypeEntity getOrCreate(PythonType pythonType) {
        if (cache.containsKey(pythonType)) {
            return cache.get(pythonType);
        }

        // Create a stable ID based on the type signature
        // We use a dummy location because the type definition is abstract/global
        // SourceLocation dummyLoc = new SourceLocation(
        // pythonType.module() != null ? pythonType.module() : "builtins",
        // 0, 0, 0, 0, 0, 0);

        // We might want to make the ID generation even more stable/explicit for types
        // For now, using the standard generator with a stable "file" path (module)
        // works.
        // But to be very safe against line number shifts (which are 0 here anyway),
        // we could just hash the type name + module.
        // However, CodeEntity.generateId uses the location.
        // Let's stick to the plan: use a deterministic pseudo-location.

        // String id = CodeEntity.generateId(dummyLoc);
        // To ensure uniqueness if multiple types map to the same dummy location
        // (unlikely if we include name in hash, but generateId only uses location),
        // we should probably NOT rely solely on generateId(loc) if loc is identical for
        // all types in a module.
        // CodeEntity.generateId implementation:
        // String content = String.format("%s:%d:%d:%d:%d", location.filePath(), ...);
        // If we use the same 0,0,0,0,0 for all types in "builtins", they will all have
        // the SAME ID!
        // This is a problem.

        // FIX: We need a way to generate a unique ID for the type.
        // We can override the ID generation or append the type name to the "filepath"
        // in the dummy location to make it unique.

        String uniqueKey = (pythonType.module() != null ? pythonType.module() : "") + "." + pythonType.name();
        SourceLocation uniqueLoc = new SourceLocation(
                uniqueKey,
                0, 0, 0, 0, 0, 0);

        TypeEntity typeEntity = new TypeEntity.Builder()
                .id(CodeEntity.generateId(uniqueLoc))
                .name(pythonType.name())
                .typeName(pythonType.name())
                .module(pythonType.module())
                .isBuiltin(pythonType.isBuiltin())
                .isCollection(pythonType.isCollection())
                .build();

        cache.put(pythonType, typeEntity);
        context.addEntity(typeEntity); // Register with context so it appears in the output

        return typeEntity;
    }
}
