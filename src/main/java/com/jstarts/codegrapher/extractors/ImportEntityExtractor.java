package com.jstarts.codegrapher.extractors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;

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
    public Optional<CodeEntity> extract(
        Node node,
        ExtractionContext context,
        String filePath,
        String sourceCode
    ) {
        boolean isFromImport = IMPORT_FROM_STATEMENT.equals(node.getType());

        String fromModule = null;
        boolean isRelative = false;
        int relativeLevel = 0;
        List<String> importedNames = new ArrayList<>();
        Map<String, String> aliases = new LinkedHashMap<>();

        if (isFromImport) {
        }




        return null;

    }

    private String extractFromModule(Node fromNode, String sourceCode) {

    }

    private int countLeadingDots(String s) {

    }

    private List<Node> childNodesByField(Node node, String fieldName) {

    }

    private List<String> extractImportList(Node parentNode, String sourceCode, Map<String, String> aliasesOut) {

    }

    private List<String> extractImportedNames(List<Node> nameNodes, String sourceCode, Map<String, String> aliasesOut) {

    }

    private String buildDisplayName(boolean isFromImport, String fromModule, List<String> names) {

    }




    
}
