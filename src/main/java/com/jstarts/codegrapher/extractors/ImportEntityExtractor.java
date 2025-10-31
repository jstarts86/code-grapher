package com.jstarts.codegrapher.extractors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.ImportEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class ImportEntityExtractor implements CodeEntityExtractor {
    private static final String IMPORT_STATEMENT = "import_statement";
    private static final String IMPORT_FROM_STATEMENT = "import_from_statement";

    @Override
    public boolean canHandle(String nodeType) {
        return IMPORT_STATEMENT.equals(nodeType) || IMPORT_FROM_STATEMENT.equals(nodeType);
    }


    // private final String fromModule;              // e.g. "pathlib" or null for plain import
    // private final boolean isFromImport;           // true if "from X import Y"
    // private final boolean isRelative;             // true if relative import ("from ..foo import bar")
    // private final int relativeLevel;              // number of leading dots for relative imports
    // private final List<String> importedNames;     // e.g. ["Path", "PurePath"] or ["os"]
    // private final Map<String, String> aliases;    // alias -> full name, e.g. { "np" : "numpy" }

//    (import_statement ; [2, 0] - [2, 18]
//      name: (aliased_import ; [2, 7] - [2, 18]
//        name: (dotted_name ; [2, 7] - [2, 12]
//          (identifier)) ; [2, 7] - [2, 12]
//        alias: (identifier))) ; [2, 16] - [2, 18]
//    (import_from_statement ; [3, 0] - [3, 34]
//      module_name: (dotted_name ; [3, 5] - [3, 12]
//        (identifier)) ; [3, 5] - [3, 12]
//      name: (dotted_name ; [3, 20] - [3, 24]
//        (identifier)) ; [3, 20] - [3, 24]
//      name: (dotted_name ; [3, 26] - [3, 34]
//        (identifier))) ; [3, 26] - [3, 34]
//    (import_from_statement ; [4, 0] - [4, 20]
//      module_name: (relative_import ; [4, 5] - [4, 7]
//        (import_prefix)) ; [4, 5] - [4, 7]
//      name: (dotted_name ; [4, 15] - [4, 20]
//        (identifier))) ; [4, 15] - [4, 20]
// import numpy as np
// from pathlib import Path, PurePath
// from .. import utils

    @Override
    public List<CodeEntity> extract(Node node, ExtractionContext context, String sourceFilePath, String sourceCode) {
        try {
            boolean isFromImport = IMPORT_FROM_STATEMENT.equals(node.getType());
            
            String fromModule = null;
            boolean isRelative = false;
            int relativeLevel = 0;
            List<String> importedNames = new ArrayList<>();
            Map<String, String> aliases = new LinkedHashMap<>();

            if (isFromImport) {
                // Extract "from X" part
                Optional<String> moduleNameOpt = extractField(node, "module_name", sourceCode);
                if (moduleNameOpt.isPresent()) {
                    String rawModule = moduleNameOpt.get();
                    
                    // Check if relative import (starts with dots)
                    if (rawModule.startsWith(".")) {
                        isRelative = true;
                        relativeLevel = countLeadingDots(rawModule);
                        fromModule = rawModule.substring(relativeLevel);
                        if (fromModule.isEmpty()) {
                            fromModule = null; // purely relative: "from .. import X"
                        }
                    } else {
                        fromModule = rawModule;
                    }
                }
                
                // Extract imported names
                extractImportedItems(node, sourceCode, importedNames, aliases);
                
            } else {
                // Plain "import X, Y as Z" statement
                extractImportedItems(node, sourceCode, importedNames, aliases);
            }

            // Build location
            SourceLocation location = buildLocation(sourceFilePath, node);

            // Build display name
            String displayName = buildDisplayName(isFromImport, fromModule, importedNames, aliases);

            // Build entity
            ImportEntity entity = new ImportEntity.Builder()
                    .id(CodeEntity.generateId(location))
                    .name(displayName)
                    .type(CodeEntityType.IMPORT)
                    .location(location)
                    .parentId(context.peekContext() != null ? context.peekContext().getId() : null)
                    .fromModule(fromModule)
                    .isFromImport(isFromImport)
                    .isRelative(isRelative)
                    .relativeLevel(relativeLevel)
                    .importedNames(importedNames)
                    .aliases(aliases)
                    .build();

            return List.of(entity);

        } catch (Exception e) {
            System.err.println("Failed to extract import from " + sourceFilePath + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Extracts all imported items (names and aliases) from an import statement.
     * Handles both plain imports and from-imports.
     */
    private void extractImportedItems(
            Node node,
            String sourceCode,
            List<String> namesOut,
            Map<String, String> aliasesOut
    ) {
        // Look for all named children
        for (int i = 0; i < node.getNamedChildCount(); i++) {
            Node child = node.getNamedChild(i);
            processImportItem(child, sourceCode, namesOut, aliasesOut);
        }
    }

    /**
     * Recursively processes import items, handling dotted_name, aliased_import, etc.
     */
    private void processImportItem(
            Node node,
            String sourceCode,
            List<String> namesOut,
            Map<String, String> aliasesOut
    ) {
        String nodeType = node.getType();

        switch (nodeType) {
            case "dotted_name":
            case "identifier":
                // Simple import: "import os" or "from X import Path"
                String name = extractText(node, sourceCode);
                namesOut.add(name);
                break;

            case "aliased_import":
                // Aliased import: "import numpy as np" or "from X import Y as Z"
                handleAliasedImport(node, sourceCode, namesOut, aliasesOut);
                break;

            case "wildcard_import":
                // "from X import *"
                namesOut.add("*");
                break;

            default:
                // Recursively check children (e.g., inside parentheses or comma-separated lists)
                for (int i = 0; i < node.getNamedChildCount(); i++) {
                    processImportItem(node.getNamedChild(i), sourceCode, namesOut, aliasesOut);
                }
                break;
        }
    }

    /**
     * Handles aliased imports: "X as Y"
     * Extracts both the original name and the alias.
     */
    private void handleAliasedImport(
            Node node,
            String sourceCode,
            List<String> namesOut,
            Map<String, String> aliasesOut
    ) {
        // Tree structure: aliased_import has two named children
        // First child: name (dotted_name or identifier)
        // Second child: alias (identifier)
        
        Node nameNode = node.getNamedChild(0);
        Node aliasNode = node.getNamedChild(1);

        if (nameNode != null && aliasNode != null) {
            String originalName = extractText(nameNode, sourceCode);
            String alias = extractText(aliasNode, sourceCode);
            
            namesOut.add(alias);  // Use alias as the primary name
            aliasesOut.put(alias, originalName);  // Map alias back to original
        }
    }

    /**
     * Counts leading dots in a string (for relative imports).
     */
    private int countLeadingDots(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '.') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Builds a human-readable display name for the import.
     */
    private String buildDisplayName(
            boolean isFromImport,
            String fromModule,
            List<String> names,
            Map<String, String> aliases
    ) {
        if (isFromImport) {
            String moduleStr = fromModule != null ? fromModule : "...";
            String namesStr = String.join(", ", names);
            return String.format("from %s import %s", moduleStr, namesStr);
        } else {
            // For plain imports, show aliases if present
            List<String> displayParts = new ArrayList<>();
            for (String name : names) {
                if (aliases.containsKey(name)) {
                    displayParts.add(aliases.get(name) + " as " + name);
                } else {
                    displayParts.add(name);
                }
            }
            return "import " + String.join(", ", displayParts);
        }
    }



    
}
