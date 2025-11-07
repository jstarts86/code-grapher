package com.jstarts.codegrapher.parsers;

import java.util.ArrayList;
import java.util.List;

import com.jstarts.codegrapher.core.entities.ClassEntity;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.FileEntity;
import com.jstarts.codegrapher.core.entities.FunctionEntity;
import com.jstarts.codegrapher.extractors.ExtractionContext;
import com.jstarts.codegrapher.extractors.ExtractorRegistry;

import ch.usi.si.seart.treesitter.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PythonTreeWalker {
    private final ExtractorRegistry registry;
    private final ExtractionContext context;

    private final String sourceFilePath;
    private final String sourceCode;

    public void walk(Node node) {
        if (node == null) {
            return;
        }

        // Get all extractors registered for this node type
        List<CodeEntity> extracted = new ArrayList<>();
        for (var extractor : registry.getExtractors(node.getType())) {
            extracted.addAll(extractor.extract(node, context, sourceFilePath, sourceCode));
        }

        // Track which entities are scoped (need push/pop)
        List<CodeEntity> scopedEntities = new ArrayList<>();

        // Process each extracted entity
        for (CodeEntity entity : extracted) {
            if (isScoped(entity)) {
                context.pushContext(entity); // pushContext internally adds it
                scopedEntities.add(entity);
            } else {
                context.addEntity(entity); // Only add non-scoped entities explicitly
            }
        }

        // Traverse children
        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i));
        }

        // Pop scoped entities in reverse order (LIFO)
        for (int i = scopedEntities.size() - 1; i >= 0; i--) {
            context.popContext();
        }
    }

    /**
     * Determines if an entity creates a new scope.
     * Only File, Class, and Function entities are scoped.
     */
    private boolean isScoped(CodeEntity entity) {
        return entity instanceof FileEntity
                || entity instanceof ClassEntity
                || entity instanceof FunctionEntity;
    }

}
