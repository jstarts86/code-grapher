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
    public Optional<CodeEntity> extract(Node node, ExtractionContext context, String filePath, String sourceCode) {
        return Optional.ofNullable(node.getChildByFieldName("name"))
                .flatMap(nameNode -> buildFunctionEntity(nameNode, node, context, filePath, sourceCode))
                .map(e -> (CodeEntity) e);
    }

    private Optional<CodeEntity> buildFunctionEntity(Node nameNode, Node functionNode, ExtractionContext context,
            String filePath, String sourceCode) {
        try {
            String name = sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
            SourceLocation location = buildLocation(filePath, functionNode);
            List<String> typeParameters = extractTypeParameters(functionNode, sourceCode);
            List<Parameter> parameters = extractParameters(functionNode, sourceCode);
            boolean isAsync = isAsync(functionNode);
            CodeEntity parent = context.peekContext();
            String parentId = parent != null ? parent.getId() : null;
            FunctionEntity functionEntity = new FunctionEntity.Builder()
                    .name(name)
                    .location(location)
                    .typeParameters(typeParameters)
                    .parameters(parameters)
                    .isAsync(isAsync)
                    .parentId(parentId)
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

    // (function_definition ; [0, 0] - [1, 8]
    // name: (identifier) ; [0, 4] - [0, 7]
    // parameters: (parameters ; [0, 7] - [0, 84]
    // (identifier) ; [0, 8] - [0, 9]
    // (typed_parameter ; [0, 11] - [0, 17]
    // (identifier) ; [0, 11] - [0, 12]
    // type: (type ; [0, 14] - [0, 17]
    // (identifier))) ; [0, 14] - [0, 17]
    // (default_parameter ; [0, 19] - [0, 23]
    // name: (identifier) ; [0, 19] - [0, 20]
    // value: (integer)) ; [0, 21] - [0, 23]
    // (typed_default_parameter ; [0, 25] - [0, 37]
    // name: (identifier) ; [0, 25] - [0, 26]
    // type: (type ; [0, 28] - [0, 31]
    // (identifier)) ; [0, 28] - [0, 31]
    // value: (string ; [0, 34] - [0, 37]
    // (string_start) ; [0, 34] - [0, 35]
    // (string_content) ; [0, 35] - [0, 36]
    // (string_end))) ; [0, 36] - [0, 37]
    // (positional_separator) ; [0, 39] - [0, 40]
    // (identifier) ; [0, 42] - [0, 43]
    // (keyword_separator) ; [0, 45] - [0, 46]
    // (typed_parameter ; [0, 48] - [0, 56]
    // (identifier) ; [0, 48] - [0, 49]
    // type: (type ; [0, 51] - [0, 56]
    // (identifier))) ; [0, 51] - [0, 56]
    // (default_parameter ; [0, 58] - [0, 66]
    // name: (identifier) ; [0, 58] - [0, 59]
    // value: (tuple ; [0, 60] - [0, 66]
    // (integer) ; [0, 61] - [0, 62]
    // (integer))) ; [0, 64] - [0, 65]
    // (list_splat_pattern ; [0, 68] - [0, 73]
    // (identifier)) ; [0, 69] - [0, 73]
    // (dictionary_splat_pattern ; [0, 75] - [0, 83]
    // (identifier))) ; [0, 77] - [0, 83]
    // body: (block ; [1, 4] - [1, 8]
    // (pass_statement))) ; [1, 4] - [1, 8]
    //
    private List<Parameter> extractParameters(Node functionNode, String sourceCode) {
        return Optional.ofNullable(functionNode.getChildByFieldName("parameters"))
                .map(Node::getChildren)
                .map(children -> children.stream()
                        .map(child -> extractSingleParameter(child, sourceCode)).toList())
                .orElse(List.of());
    }

    private Parameter extractSingleParameter(Node paramNode, String sourceCode) {
        return switch (paramNode.getType()) {
            case "identifier" -> new Parameter(
                    ParameterKind.NORMAL,
                    extractText(paramNode, sourceCode),
                    Optional.empty(),
                    Optional.empty());

            case "typed_parameter" -> {
                String[] parts = paramNode.getChildren().stream()
                        .collect(Collector.of(
                                () -> new String[2],
                                (acc, child) -> {
                                    switch (child.getType()) {
                                        case "identifier" -> acc[0] = extractText(child, sourceCode);
                                        case "type" -> acc[1] = extractText(child, sourceCode);
                                    }
                                },
                                (a, b) -> a));
                yield new Parameter(ParameterKind.TYPED, parts[0], Optional.ofNullable(parts[1]), Optional.empty());
            }
            // name: (identifier) ; [0, 19] - [0, 20]
            // value: (integer)) ; [0, 21] - [0, 23]
            case "default_parameter" -> {
                yield new Parameter(
                        ParameterKind.DEFAULT,
                        extractField(paramNode, "name", sourceCode).orElse(null),
                        Optional.empty(),
                        extractField(paramNode, "value", sourceCode));
            }
            case "typed_default_parameter" -> {
                yield new Parameter(ParameterKind.TYPED_DEFAULT,
                        extractField(paramNode, "name", sourceCode).orElse(null),
                        extractField(paramNode, "type", sourceCode),
                        extractField(paramNode, "value", sourceCode));

            }
            case "list_splat_pattern" -> {
                Optional<Node> target = paramNode.getChildren().stream()
                        .filter(child -> !"*".equals(child.getType()))
                        .findFirst();
                yield new Parameter(ParameterKind.LIST_SPLAT,
                        target.map(child -> extractText(child, sourceCode)).orElse(null),
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
                        Optional.empty());
            }
            default -> new Parameter(ParameterKind.DEFAULT,
                    "null",
                    Optional.of("null"),
                    Optional.of("null"));
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
