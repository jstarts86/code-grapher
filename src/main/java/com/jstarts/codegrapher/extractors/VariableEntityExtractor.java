package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.*;
import ch.usi.si.seart.treesitter.Node;
import java.util.*;

/**
 * Extracts variable declarations and assignments (both typed and untyped)
 * from Python AST nodes produced by Tree-sitter.
 *
 * Supported node types: "assignment", "typed_assignment"
 */
public class VariableEntityExtractor implements CodeEntityExtractor {

    private static final Set<String> SUPPORTED_TYPES = Set.of("assignment", "typed_assignment");

    @Override
    public boolean canHandle(String nodeType) {
        return SUPPORTED_TYPES.contains(nodeType);
    }

    @Override
    public List<CodeEntity> extract(
            Node node,
            ExtractionContext context,
            String filePath,
            String sourceCode) {

        List<CodeEntity> vars = new ArrayList<>();
        CodeEntity currentParent = context.peekContext();
        if (currentParent.getType() == CodeEntityType.CLASS) {
            return List.of();
        }

        // LHS extraction
        Node left = node.getChildByFieldName("left");
        if (left == null)
            return vars;

        List<Node> identifiers = new ArrayList<>();
        collectIdentifiers(left, identifiers);

        // Optional type info
        String declaredType = extractField(node, "type", sourceCode).orElse(null);
        boolean isTyped = declaredType != null;

        for (Node idNode : identifiers) {
            String name = extractText(idNode, sourceCode);
            SourceLocation loc = buildLocation(filePath, idNode);

            VariableEntity variable = new VariableEntity.Builder()
                    .id(CodeEntity.generateId(loc))
                    .type(CodeEntityType.VARIABLE)
                    .name(name)
                    .declaredType(declaredType)
                    .isTyped(isTyped)
                    .isAssigned(true)
                    .isParameterLike(false)
                    .location(loc)
                    .parentId(context.peekContext() != null
                            ? context.peekContext().getId()
                            : null)
                    .build();

            vars.add(variable);
            if (isTyped && declaredType != null && !declaredType.isBlank()) {
                // Construct a canonical TypeEntity for this annotation
                PythonType pyType = PythonType.of(declaredType);

                // Use the cache to get or create the TypeEntity

                // (You will later add the HAS_TYPE edge during Pass 2)

                // (You will later add the HAS_TYPE edge during Pass 2)
            }
        }

        return vars;
    }

    /**
     * Recursively collects identifiers appearing on the left-hand side
     * of an assignment (e.g., x, y, z in "x, (y, z) = foo()").
     */
    private void collectIdentifiers(Node node, List<Node> collected) {
        if (node == null)
            return;
        switch (node.getType()) {
            case "identifier" -> collected.add(node);
            case "tuple", "list" -> {
                for (int i = 0; i < node.getChildCount(); i++) {
                    collectIdentifiers(node.getChild(i), collected);
                }
            }
            default -> {
                /* ignore */ }
        }
    }
}
