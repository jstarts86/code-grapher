package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.*;
import com.jstarts.codegrapher.parsers.PythonTypeParser;

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
        PythonTypeParser pythonTypeParser = new PythonTypeParser(context.getTypeCanon());

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
        Node typeNode = node.getChildByFieldName("type");
        String typeId = pythonTypeParser.parse(typeNode, sourceCode).getId();

        for (Node idNode : identifiers) {
            String name = extractText(idNode, sourceCode);
            SourceLocation loc = buildLocation(filePath, idNode);

            VariableEntity variable = new VariableEntity.Builder()
                    .id(CodeEntity.generateId(loc))
                    .type(CodeEntityType.VARIABLE)
                    .name(name)
                    .declaredType(declaredType)
                    .typeId(typeId)
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
