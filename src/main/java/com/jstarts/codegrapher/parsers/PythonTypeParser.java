package com.jstarts.codegrapher.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jstarts.codegrapher.core.entities.PythonTypeCanon;
import com.jstarts.codegrapher.core.entities.PythonTypeEntity;

import ch.usi.si.seart.treesitter.Node;

public class PythonTypeParser {

    private final PythonTypeCanon typeCanon;

    public PythonTypeParser(PythonTypeCanon typeCanon) {
        this.typeCanon = typeCanon;
    }

    public PythonTypeEntity parse(Node typeNode, String sourceCode) {
        if (typeNode == null) {
            return null;
        }
        String nodeType = typeNode.getType();

        switch (nodeType) {
            case "type":
                if (typeNode.getChildCount() == 1) {
                    return parse(typeNode.getChild(0), sourceCode);
                }
                return parseSimpleType(typeNode, sourceCode);
            case "identifier":
            case "member_type": // e.g. typing.List
                return parseSimpleType(typeNode, sourceCode);
            case "string":
                // Handle forward references like "Node"
                String text = extractText(typeNode, sourceCode);
                String cleaned = text.replaceAll("^['\"]|['\"]$", "");
                return createTypeEntity(cleaned, cleaned, List.of());
            case "generic_type":
                return parseGenericType(typeNode, sourceCode);
            case "subscript":
                // list[int] can be parsed as subscript in some contexts
                return parseGenericType(typeNode, sourceCode);
            case "union_type":
                return parseUnionType(typeNode, sourceCode);
            case "binary_operator":
                // handle A | B if parsed as binary_operator (though union_type is preferred in
                // grammar)
                if ("|".equals(typeNode.getChildByFieldName("operator").getType())) {
                    return parseUnionType(typeNode, sourceCode);
                }
                return parseSimpleType(typeNode, sourceCode);
            case "list":
                // Handle [int, str] in Callable[[int, str], int]
                return parseListType(typeNode, sourceCode);
            case "splat_type":
            case "constrained_type":
            default:
                return parseSimpleType(typeNode, sourceCode);
        }
    }

    private PythonTypeEntity parseSimpleType(Node node, String sourceCode) {
        String name = extractText(node, sourceCode);
        return createTypeEntity(name, name, List.of());
    }

    private PythonTypeEntity parseGenericType(Node node, String sourceCode) {
        // generic_type: identifier type_parameter
        // subscript: value [ subscript_list ]

        Node baseNode = node.getChild(0); // identifier or value
        String baseName = extractText(baseNode, sourceCode);

        List<PythonTypeEntity> generics = new ArrayList<>();

        // Try to find the arguments node
        Node argsNode = null;
        if ("generic_type".equals(node.getType())) {
            argsNode = node.getChild(1); // type_parameter
        } else if ("subscript".equals(node.getType())) {
            // subscript children: value, [, subscript_list, ]
            // We want the node that contains the list of types.
            // In tree-sitter-python, subscript usually has a child for the index/slice.
            // Let's iterate children to find the content between brackets
            for (int i = 0; i < node.getChildCount(); i++) {
                Node child = node.getChild(i);
                if (!child.equals(baseNode) && !"[".equals(child.getType()) && !"]".equals(child.getType())) {
                    argsNode = child;
                    break;
                }
            }
        }

        if (argsNode != null) {
            // If argsNode is a list of comma-separated types (like in type_parameter or
            // subscript)
            // We need to iterate its children.
            // type_parameter children: [, type, comma, type, ] -- wait, type_parameter IS
            // the wrapper
            // If argsNode is "type_parameter", it wraps the types.

            int childCount = argsNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                Node child = argsNode.getChild(i);
                // Skip punctuation
                if (",".equals(child.getType()) || "[".equals(child.getType()) || "]".equals(child.getType())) {
                    continue;
                }
                PythonTypeEntity argType = parse(child, sourceCode);
                if (argType != null) {
                    generics.add(argType);
                }
            }

            // If argsNode was just a single node (like in subscript value[index]), and it
            // wasn't a list/tuple wrapper
            // but we treated it as the container, we might need to check if we should have
            // parsed IT.
            // Actually, for subscript `list[int]`, the index is `int`.
            // For `dict[str, int]`, the index is usually a `tuple` or just comma separated
            // nodes?
            // In python grammar: subscript: value [ commaSep1(subscript) ]
            // So the children of subscript node are: value, [, sub1, comma, sub2, ]
            // My logic above `argsNode = child` would pick `sub1`.
            // I should iterate the subscript node itself for children after `[` and before
            // `]`.

            if ("subscript".equals(node.getType())) {
                generics.clear(); // Reset
                boolean insideBrackets = false;
                for (int i = 0; i < node.getChildCount(); i++) {
                    Node child = node.getChild(i);
                    if ("[".equals(child.getType())) {
                        insideBrackets = true;
                        continue;
                    }
                    if ("]".equals(child.getType())) {
                        insideBrackets = false;
                        break;
                    }
                    if (insideBrackets && !",".equals(child.getType())) {
                        PythonTypeEntity argType = parse(child, sourceCode);
                        if (argType != null) {
                            generics.add(argType);
                        }
                    }
                }
            }
        }

        String signature = buildSignature(baseName, generics);
        return createTypeEntity(baseName, signature, generics);
    }

    private PythonTypeEntity parseUnionType(Node node, String sourceCode) {
        // union_type: type | type
        // Flatten A | B | C
        List<PythonTypeEntity> parts = new ArrayList<>();
        collectUnionParts(node, sourceCode, parts);

        String signature = parts.stream()
                .map(PythonTypeEntity::getSignature)
                .collect(Collectors.joining(" | "));

        return createTypeEntity("Union", signature, parts);
    }

    private void collectUnionParts(Node node, String sourceCode, List<PythonTypeEntity> parts) {
        if ("union_type".equals(node.getType()) ||
                ("binary_operator".equals(node.getType())
                        && "|".equals(node.getChildByFieldName("operator").getType()))) {

            Node left;
            Node right;
            if ("union_type".equals(node.getType())) {
                // union_type children: type, |, type
                left = node.getChild(0);
                right = node.getChild(2);
            } else {
                left = node.getChildByFieldName("left");
                right = node.getChildByFieldName("right");
            }

            collectUnionParts(left, sourceCode, parts);
            collectUnionParts(right, sourceCode, parts);
        } else {
            parts.add(parse(node, sourceCode));
        }
    }

    private PythonTypeEntity parseListType(Node node, String sourceCode) {
        // Handle [int, str] as a type (e.g. in Callable)
        List<PythonTypeEntity> elements = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChild(i);
            if ("[".equals(child.getType()) || "]".equals(child.getType()) || ",".equals(child.getType())) {
                continue;
            }
            elements.add(parse(child, sourceCode));
        }

        String signature = "[" + elements.stream()
                .map(PythonTypeEntity::getSignature)
                .collect(Collectors.joining(", ")) + "]";

        return createTypeEntity("ListLiteral", signature, elements);
    }

    private PythonTypeEntity createTypeEntity(String name, String signature, List<PythonTypeEntity> generics) {
        return typeCanon.getCanonicalType(signature, () -> new PythonTypeEntity.Builder()
                .id(org.apache.commons.codec.digest.DigestUtils.sha256Hex(signature).substring(0, 16))
                .name(name)
                .signature(signature)
                .generics(generics)
                .build());
    }

    private String buildSignature(String baseName, List<PythonTypeEntity> generics) {
        if (generics.isEmpty()) {
            return baseName;
        }
        return baseName + "[" + generics.stream()
                .map(PythonTypeEntity::getSignature)
                .collect(Collectors.joining(", ")) + "]";
    }

    private String extractText(Node node, String sourceCode) {
        if (node == null)
            return "";
        return sourceCode.substring(node.getStartByte(), node.getEndByte());
    }
}
