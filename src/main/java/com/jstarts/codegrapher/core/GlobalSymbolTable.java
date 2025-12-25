package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalSymbolTable {

    private final Map<String, CodeEntity> symbolMap = new HashMap<>();
    private final Path rootPath;

    public GlobalSymbolTable(List<CodeEntity> entities, Path rootPath) {
        this.rootPath = rootPath.toAbsolutePath().normalize();
        buildIndex(entities);
    }

    private void buildIndex(List<CodeEntity> entities) {
        // First pass: Index Files and Packages
        for (CodeEntity entity : entities) {
            if (entity instanceof FileEntity || entity instanceof PackageEntity) {
                String qName = deriveQualifiedName(entity);
                if (qName != null) {
                    symbolMap.put(qName, entity);
                }
            }
        }

        // Second pass: Index Classes and Functions (children of files/classes)
        // We need to look up parents to build the full qualified name
        Map<String, CodeEntity> idMap = new HashMap<>();
        entities.forEach(e -> idMap.put(e.getId(), e));

        for (CodeEntity entity : entities) {
            if (entity instanceof ClassEntity || entity instanceof FunctionEntity) {
                String qName = buildQualifiedNameRecursive(entity, idMap);
                if (qName != null) {
                    symbolMap.put(qName, entity);
                }
            }
        }
    }

    public String deriveQualifiedName(CodeEntity entity) {
        if (entity.getLocation() == null || entity.getLocation().filePath() == null) {
            return null;
        }

        Path path = Path.of(entity.getLocation().filePath()).toAbsolutePath().normalize();
        if (!path.startsWith(rootPath)) {
            return null;
        }

        Path relative = rootPath.relativize(path);
        String pathStr = relative.toString();

        // Remove .py extension
        if (pathStr.endsWith(".py")) {
            pathStr = pathStr.substring(0, pathStr.length() - 3);
        }

        // Do NOT remove __init__ automatically, so we can distinguish package dir from
        // init file
        // if (pathStr.endsWith("__init__")) { ... }

        return pathStr.replace('/', '.').replace('\\', '.');
    }

    private String buildQualifiedNameRecursive(CodeEntity entity, Map<String, CodeEntity> idMap) {
        if (entity instanceof FileEntity || entity instanceof PackageEntity) {
            return deriveQualifiedName(entity);
        }

        if (entity.getParentId() == null) {
            return entity.getName(); // Should not happen for Class/Func in valid graph
        }

        CodeEntity parent = idMap.get(entity.getParentId());
        if (parent == null) {
            return entity.getName();
        }

        String parentQName = buildQualifiedNameRecursive(parent, idMap);
        if (parentQName == null || parentQName.isEmpty()) {
            return entity.getName();
        }

        return parentQName + "." + entity.getName();
    }

    public CodeEntity lookup(String qualifiedName) {
        return symbolMap.get(qualifiedName);
    }

    public Map<String, CodeEntity> getSymbolMap() {
        return symbolMap;
    }
}
