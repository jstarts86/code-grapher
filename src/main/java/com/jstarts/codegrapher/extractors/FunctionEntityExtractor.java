package com.jstarts.codegrapher.extractors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.FunctionEntity;
import com.jstarts.codegrapher.core.entities.FunctionEntity.Parameter;
import com.jstarts.codegrapher.core.entities.FunctionEntity.ParameterKind;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class FunctionEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        return "function_definition".equals(nodeType);
    }

    @Override
    public List<CodeEntity> extract(Node node, ExtractionContext context, String filePath, String sourceCode) {
        com.jstarts.codegrapher.parsers.PythonTypeParser pythonTypeParser = new com.jstarts.codegrapher.parsers.PythonTypeParser(
                context.getTypeCanon());
        return Optional.ofNullable(node.getChildByFieldName("name"))
                .flatMap(nameNode -> buildFunctionEntity(nameNode, node, context, filePath, sourceCode,
                        pythonTypeParser))
                .<List<CodeEntity>>map(List::of)
                .orElse(List.of());
    }

    private Optional<CodeEntity> buildFunctionEntity(Node nameNode, Node functionNode, ExtractionContext context,
            String filePath, String sourceCode, com.jstarts.codegrapher.parsers.PythonTypeParser parser) {
        try {
            String name = sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
            SourceLocation location = buildLocation(filePath, functionNode);
            List<String> typeParameters = extractTypeParameters(functionNode, sourceCode);
            List<Parameter> parameters = extractParameters(functionNode, sourceCode, parser);
            boolean isAsync = isAsync(functionNode);
            CodeEntity parent = context.peekContext();
            String parentId = parent != null ? parent.getId() : null;

            String returnType = null;
            String returnTypeId = null;
            Node returnTypeNode = functionNode.getChildByFieldName("return_type");
            if (returnTypeNode != null) {
                com.jstarts.codegrapher.core.entities.PythonTypeEntity typeEntity = parser.parse(returnTypeNode,
                        sourceCode);
                if (typeEntity != null) {
                    returnType = typeEntity.getSignature(); // or name?
                    returnTypeId = typeEntity.getId();
                }
            }

            FunctionEntity functionEntity = new FunctionEntity.Builder()
                    .id(CodeEntity.generateId(location))
                    .name(name)
                    .location(location)
                    .typeParameters(typeParameters)
                    .parameters(parameters)
                    .isAsync(isAsync)
                    .parentId(parentId)
                    .returnType(returnType)
                    .returnTypeId(returnTypeId)
                    .build();
            return Optional.of(functionEntity);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private List<String> extractTypeParameters(Node functionNode, String sourceCode) {
        return Optional.ofNullable(functionNode.getChildByFieldName("type_parameters"))
                .map(Node::getChildren)
                .map(children -> children.stream()
                        .filter(child -> "type".equals(child.getType()))
                        .map(child -> sourceCode.substring(child.getStartByte(), child.getEndByte()))
                        .toList())
                .orElse(List.of());
    }

    private List<Parameter> extractParameters(Node functionNode, String sourceCode,
            com.jstarts.codegrapher.parsers.PythonTypeParser parser) {
        return Optional.ofNullable(functionNode.getChildByFieldName("parameters"))
                .map(Node::getChildren)
                .map(children -> children.stream()
                        .filter(child -> !child.getType().equals("(") && !child.getType().equals(")")
                                && !child.getType().equals(","))
                        .map(child -> extractSingleParameter(child, sourceCode, parser)).toList())
                .orElse(List.of());
    }

    private Parameter extractSingleParameter(Node paramNode, String sourceCode,
            com.jstarts.codegrapher.parsers.PythonTypeParser parser) {
        return switch (paramNode.getType()) {
            case "identifier" -> new Parameter(
                    ParameterKind.NORMAL,
                    extractText(paramNode, sourceCode),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());

            case "typed_parameter" -> {
                String name = null;
                String typeName = null;
                String typeId = null;

                for (int i = 0; i < paramNode.getChildCount(); i++) {
                    Node child = paramNode.getChild(i);
                    if ("identifier".equals(child.getType()) || "list_splat_pattern".equals(child.getType())
                            || "dictionary_splat_pattern".equals(child.getType())) {
                        name = extractText(child, sourceCode);
                    } else if ("type".equals(child.getType())) {
                        com.jstarts.codegrapher.core.entities.PythonTypeEntity typeEntity = parser.parse(child,
                                sourceCode);
                        if (typeEntity != null) {
                            typeName = typeEntity.getSignature();
                            typeId = typeEntity.getId();
                        } else {
                            typeName = extractText(child, sourceCode);
                        }
                    }
                }
                yield new Parameter(ParameterKind.TYPED, name, Optional.ofNullable(typeName), Optional.empty(),
                        Optional.ofNullable(typeId));
            }
            // name: (identifier) ; [0, 19] - [0, 20]
            // value: (integer)) ; [0, 21] - [0, 23]
            case "default_parameter" -> {
                yield new Parameter(
                        ParameterKind.DEFAULT,
                        extractField(paramNode, "name", sourceCode).orElse(null),
                        Optional.empty(),
                        extractField(paramNode, "value", sourceCode),
                        Optional.empty());
            }
            case "typed_default_parameter" -> {
                String name = extractField(paramNode, "name", sourceCode).orElse(null);
                String value = extractField(paramNode, "value", sourceCode).orElse(null);
                String typeName = null;
                String typeId = null;

                Node typeNode = paramNode.getChildByFieldName("type");
                if (typeNode != null) {
                    com.jstarts.codegrapher.core.entities.PythonTypeEntity typeEntity = parser.parse(typeNode,
                            sourceCode);
                    if (typeEntity != null) {
                        typeName = typeEntity.getSignature();
                        typeId = typeEntity.getId();
                    } else {
                        typeName = extractText(typeNode, sourceCode);
                    }
                }

                yield new Parameter(ParameterKind.TYPED_DEFAULT,
                        name,
                        Optional.ofNullable(typeName),
                        Optional.ofNullable(value),
                        Optional.ofNullable(typeId));

            }
            case "list_splat_pattern" -> {
                Optional<Node> target = paramNode.getChildren().stream()
                        .filter(child -> !"*".equals(child.getType()))
                        .findFirst();
                yield new Parameter(ParameterKind.LIST_SPLAT,
                        target.map(child -> extractText(child, sourceCode)).orElse(null),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty());
            }

            case "dictionary_splat_pattern" -> {
                Optional<Node> target = paramNode.getChildren().stream()
                        .filter(child -> !"**".equals(child.getType()))
                        .findFirst();
                yield new Parameter(ParameterKind.DICT_SPLAT,
                        target.map(child -> extractText(child, sourceCode)).orElse(null),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty());
            }
            default -> new Parameter(ParameterKind.DEFAULT,
                    "null",
                    Optional.of("null"),
                    Optional.of("null"),
                    Optional.empty());
        };

    }

    private boolean isAsync(Node functionNode) {
        return functionNode.getChildren().stream()
                .anyMatch(child -> "async".equals(child.getType()));
    }

    // private List<String> extractTypeParameters(Node functionNode, String
    // sourceCode) {
    // return
    // Optional.ofNullable(functionNode.getChildByFieldName("type_parameters"))
    // .map(Node::getChildren)
    // }

}
