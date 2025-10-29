package com.jstarts.codegrapher.extractors;

import java.util.Optional;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public interface CodeEntityExtractor {
    boolean canHandle(String nodeType);

    Optional<CodeEntity> extract(Node node, ExtractionContext context, String sourceFilePath, String sourceCode);

    default SourceLocation buildLocation(String filePath, Node node) {
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
