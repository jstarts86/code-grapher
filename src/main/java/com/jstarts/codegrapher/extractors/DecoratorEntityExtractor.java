package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.*;
import ch.usi.si.seart.treesitter.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts decorator information from "decorated_definition" and "decorator"
 * nodes.
 * Produces one DecoratorEntity per decorator attached to a class or function.
 */
public class DecoratorEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        // Handle the wrapper "decorated_definition"; the inner "decorator" nodes
        // are processed within extract().
        return "decorated_definition".equals(nodeType);
    }

    @Override
    public List<CodeEntity> extract(
            Node node,
            ExtractionContext context,
            String filePath,
            String sourceCode) {

        List<CodeEntity> decorators = new ArrayList<>();

        // Find all decorator children
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Node child = node.getChild(i);
            if (!"decorator".equals(child.getType()))
                continue;

            // Extract decorator expression text
            Node exprNode = child.getChildByFieldName("expression");
            String exprText = exprNode != null
                    ? extractText(exprNode, sourceCode)
                    : extractText(child, sourceCode);

            SourceLocation loc = buildLocation(filePath, child);

            DecoratorEntity dec = new DecoratorEntity.Builder()
                    .id(CodeEntity.generateId(loc))
                    .type(CodeEntityType.DECORATOR)
                    .expression(exprText)
                    .location(loc)
                    // Set parent: the enclosing Function or Class in the context
                    .parentId(context.peekContext() != null
                            ? context.peekContext().getId()
                            : null)
                    .build();

            decorators.add(dec);
        }

        // Note: the field("definition") part (class/function) will be visited
        // separately by its respective extractor via PythonTreeWalker.
        return decorators;
    }
}
