package com.jstarts.codegrapher.extractors;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class FunctionEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        return "function_definition".equals(nodeType);
    }

    @Override
    public Optional<CodeEntity> extract(Node node, ExtractionContext context, String filePath, String sourceCode) {
        return buildFunctionEntity(node, context, filePath)
                .map(entity -> (CodeEntity) entity);
    }

    private Optional<CodeEntity> buildFunctionEntity(Node nameNode, Node functionNode, ExtractionContext context,
            String filePath, String sourceCode) {
        try {
            String name = sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
            SourceLocation location = buildLocation(filePath, functionNode);

        } catch (Exception e) {
            // TODO: handle exception

        }
    }

    private Optional<boolean> extractIsAsync(Node functionNode,)

    // private List<String> extractTypeParameters(Node functionNode, String
    // sourceCode) {
    // return
    // Optional.ofNullable(functionNode.getChildByFieldName("type_parameters"))
    // .map(Node::getChildren)
    // }

    private SourceLocation buildLocation(String filePath, Node node) {
        return SourceLocation.builder()
                .filePath(filePath)
                .startLine(node.getStartPoint().getRow() + 1)
                .endLine(node.getEndPoint().getRow() + 1)
                .startByte(node.getStartByte())
                .endByte(node.getEndByte())
                .startCol(node.getStartPoint().getColumn())
                .endCol(node.getEndPoint().getColumn())
                .build();
    }
}
