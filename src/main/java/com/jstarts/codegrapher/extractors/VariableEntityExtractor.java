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

        // Determine scope
        VariableEntity.ScopeKind scope = VariableEntity.ScopeKind.LOCAL;
        if (currentParent == null || currentParent.getType() == CodeEntityType.PACKAGE) { // Should be FileEntity
                                                                                          // usually
            // If parent is file, it's global
            scope = VariableEntity.ScopeKind.GLOBAL;
        } else if (currentParent instanceof FileEntity) {
            scope = VariableEntity.ScopeKind.GLOBAL;
        } else if (currentParent instanceof ClassEntity) {
            scope = VariableEntity.ScopeKind.CLASS_FIELD;
        } else if (currentParent instanceof FunctionEntity) {
            scope = VariableEntity.ScopeKind.LOCAL;
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
        PythonTypeEntity typeEntity = pythonTypeParser.parse(typeNode, sourceCode);
        String typeId = typeEntity != null ? typeEntity.getId() : null;

        for (Node idNode : identifiers) {
            String name = extractText(idNode, sourceCode);
            SourceLocation loc = buildLocation(filePath, idNode);

            VariableEntity.ScopeKind currentScope = scope;

            // Handle instance fields (self.x)
            if (idNode.getType().equals("attribute")) {
                Node object = idNode.getChildByFieldName("object");
                Node attribute = idNode.getChildByFieldName("attribute");
                if (object != null && "self".equals(extractText(object, sourceCode))) {
                    currentScope = VariableEntity.ScopeKind.INSTANCE_FIELD;
                    name = extractText(attribute, sourceCode); // Use "x" instead of "self.x"
                    // Adjust location to point to "x"
                    loc = buildLocation(filePath, attribute);
                } else {
                    // Other attributes like "other.x = 1", we might treat as variable "other.x" or
                    // ignore?
                    // For now, let's keep full name "other.x" and scope as LOCAL (it's a
                    // modification of an object in local scope)
                }
            }

            VariableEntity variable = new VariableEntity.Builder()
                    .id(CodeEntity.generateId(loc))
                    .type(CodeEntityType.VARIABLE)
                    .name(name)
                    .declaredType(declaredType)
                    .typeId(typeId)
                    .isTyped(isTyped)
                    .isAssigned(true)
                    .isParameterLike(false)
                    .scope(currentScope)
                    .location(loc)
                    .parentId(context.peekContext() != null
                            ? context.peekContext().getId()
                            : null)
                    .build();

            vars.add(variable);
        }

        return vars;
    }

    /**
     * Recursively collects identifiers appearing on the left-hand side
     * of an assignment.
     */
    private void collectIdentifiers(Node node, List<Node> collected) {
        if (node == null)
            return;
        switch (node.getType()) {
            case "identifier" -> collected.add(node);
            case "attribute" -> collected.add(node); // Handle self.x
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
