package com.jstarts.codegrapher.graph.parser.extractors;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.AnnotationDef;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;

import ch.usi.si.seart.treesitter.Node;

public class AnnotationExtractor implements CodeEntityExtractor {

    @Override
    public void extract(Node annotationNode, String sourceCode, String filePath, String packageName,
            GraphBuilder graphBuilder) {
        String queryStr = """
                (marker_annotation name: (identifier) @name) @full
                (annotation name: (identifier) @name) @full
                (annotation name: (scoped_identifier) @name) @full
                """;
        Node nameNode = annotationNode.getChildByFieldName("name");

        if (nameNode != null) {
            String annotationName = sourceCode.substring(annotationNode.getStartByte(), annotationNode.getEndByte());

            String fqn = packageName.isEmpty() ? annotationName : packageName + "." + annotationName;

            SourceLocation location = SourceLocation.builder()
                    .filePath(filePath)
                    .startLine(nameNode.getStartPoint().getRow() + 1)
                    .endLine(nameNode.getEndPoint().getRow() + 1)
                    .startByte(nameNode.getStartByte())
                    .endByte(nameNode.getEndByte())
                    .startCol(nameNode.getStartPoint().getColumn())
                    .endCol(nameNode.getEndPoint().getColumn())
                    .build();
            ;

            AnnotationDef annotationDef = new AnnotationDef(fqn, location);
            graphBuilder.registerNode(annotationDef);
        }

    }

}
