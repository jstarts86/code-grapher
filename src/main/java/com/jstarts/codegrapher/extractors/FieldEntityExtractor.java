package com.jstarts.codegrapher.extractors;

import java.util.ArrayList;
import java.util.List;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.FieldEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class FieldEntityExtractor implements CodeEntityExtractor {
    @Override
    public boolean canHandle(String nodeType) {
        return nodeType.equals("assignment");
    }

    public List<CodeEntity> extract(
            Node node,
            ExtractionContext context,
            String filePath,
            String sourceCode) {

        // If we aren't inside a class, do nothing (avoid misclassification)
        CodeEntity currentParent = context.peekContext();
        if (currentParent == null
                || currentParent.getType() != CodeEntityType.CLASS) {
            return List.of();
        }

        List<CodeEntity> results = new ArrayList<>();

        Node left = node.getChildByFieldName("left");
        if (left == null) return results;

        List<Node> ids = new ArrayList<>();
        collectIdentifiers(left, ids);

        String declaredType = extractField(node, "type", sourceCode).orElse(null);
        boolean isTyped = declaredType != null;

        for (Node idNode : ids) {
            String name = extractText(idNode, sourceCode);
            SourceLocation loc = buildLocation(filePath, idNode);

            FieldEntity field = new FieldEntity.Builder()
                    .id(CodeEntity.generateId(loc))
                    .type(CodeEntityType.FIELD)
                    .name(name)
                    .declaredType(declaredType)
                    .isTyped(isTyped)
                    .isAssigned(true)
                    .isClassVariable(true)
                    .location(loc)
                    .parentId(currentParent.getId())
                    .build();

            results.add(field);
        }

        return results;
    }
    private void collectIdentifiers(Node node, List<Node> result) {
        if (node == null) return;
        switch (node.getType()) {
            case "identifier" -> result.add(node);
            case "tuple", "list" -> {
                for (int i = 0; i < node.getChildCount(); i++) {
                    collectIdentifiers(node.getChild(i), result);
                }
            }
            default -> {
                /* ignore */
            }
        }
    }
      

    
}
