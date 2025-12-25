package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportResolver {

    private final GlobalSymbolTable symbolTable;

    public ImportResolver(GlobalSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void resolveImports(List<CodeEntity> entities) {
        // Multi-pass resolution to handle dependencies (e.g. re-exports)
        boolean changed = true;
        int maxPasses = 5;
        for (int i = 0; i < maxPasses && changed; i++) {
            changed = false;
            for (CodeEntity entity : entities) {
                if (entity instanceof ImportEntity) {
                    if (resolveImport((ImportEntity) entity, entities)) {
                        changed = true;
                    }
                }
            }
        }
    }

    private boolean resolveImport(ImportEntity importEntity, List<CodeEntity> allEntities) {
        Map<String, String> resolutions = new HashMap<>();
        if (importEntity.getResolvedReferences() != null) {
            resolutions.putAll(importEntity.getResolvedReferences());
        }

        boolean changed = false;

        // Find the file containing this import to resolve relative imports
        Optional<FileEntity> sourceFile = findSourceFile(importEntity, allEntities);

        // 1. Resolve "from X import Y"
        if (importEntity.isFromImport()) {
            String baseModule = resolveBaseModule(importEntity, sourceFile);

            if (baseModule != null) {
                for (String name : importEntity.getImportedNames()) {
                    if (resolutions.containsKey(name))
                        continue; // Already resolved

                    String fullQName = baseModule + "." + name;
                    CodeEntity target = symbolTable.lookup(fullQName);

                    if (target != null) {
                        resolutions.put(name, target.getId());
                        changed = true;
                    } else {
                        // Check for re-exports in __init__.py if baseModule is a package
                        CodeEntity baseEntity = symbolTable.lookup(baseModule);
                        if (baseEntity instanceof PackageEntity) {
                            String initQName = baseModule + ".__init__";
                            CodeEntity initFile = symbolTable.lookup(initQName);
                            if (initFile != null) {
                                // Find imports in initFile that import 'name'
                                Optional<String> reExportedId = findReExport(initFile, name, allEntities);
                                if (reExportedId.isPresent()) {
                                    resolutions.put(name, reExportedId.get());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        // 2. Resolve "import X"
        else {
            for (String name : importEntity.getImportedNames()) {
                if (resolutions.containsKey(name))
                    continue;

                CodeEntity target = symbolTable.lookup(name);
                if (target != null) {
                    resolutions.put(name, target.getId());
                    changed = true;
                }
            }
        }

        if (changed) {
            importEntity.setResolvedReferences(resolutions);
        }
        return changed;
    }

    private Optional<String> findReExport(CodeEntity initFile, String name, List<CodeEntity> allEntities) {
        // Look for ImportEntity in initFile that imports 'name'
        return allEntities.stream()
                .filter(e -> e instanceof ImportEntity && initFile.getId().equals(e.getParentId()))
                .map(e -> (ImportEntity) e)
                .filter(imp -> imp.getResolvedReferences() != null && imp.getResolvedReferences().containsKey(name))
                .map(imp -> imp.getResolvedReferences().get(name))
                .findFirst();
    }

    private Optional<FileEntity> findSourceFile(ImportEntity importEntity, List<CodeEntity> allEntities) {
        String parentId = importEntity.getParentId();
        while (parentId != null) {
            String currentId = parentId;
            Optional<CodeEntity> parent = allEntities.stream().filter(e -> e.getId().equals(currentId)).findFirst();
            if (parent.isPresent()) {
                if (parent.get() instanceof FileEntity) {
                    return Optional.of((FileEntity) parent.get());
                }
                parentId = parent.get().getParentId();
            } else {
                break;
            }
        }
        return Optional.empty();
    }

    private String resolveBaseModule(ImportEntity importEntity, Optional<FileEntity> sourceFile) {
        if (importEntity.isRelative()) {
            if (sourceFile.isEmpty())
                return null;

            String sourceQName = symbolTable.deriveQualifiedName(sourceFile.get());
            if (sourceQName == null)
                return null;

            String[] parts = sourceQName.split("\\.");
            int level = importEntity.getRelativeLevel();

            if (parts.length < level)
                return null;

            StringBuilder base = new StringBuilder();
            for (int i = 0; i < parts.length - level; i++) {
                if (i > 0)
                    base.append(".");
                base.append(parts[i]);
            }

            String suffix = importEntity.getFromModule();
            if (suffix != null && !suffix.isEmpty()) {
                if (base.length() > 0)
                    base.append(".");
                base.append(suffix);
            }

            return base.toString();
        } else {
            return importEntity.getFromModule();
        }
    }
}
