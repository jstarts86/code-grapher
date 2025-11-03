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
        List<CodeEntity> extracted = registry.getExtractor(node.getType())
                .map(extractor -> extractor.extract(node, context, sourceFilePath, sourceCode))
                .orElse(List.of());
        // Track which entities are scoped (need push/pop)
        List<CodeEntity> scopedEntities = new ArrayList<>();

        // Process each extracted entity
        for (CodeEntity entity : extracted) {
            context.addEntity(entity); // Always add to collection

            if (isScoped(entity)) {
                context.pushContext(entity);
                scopedEntities.add(entity);
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
