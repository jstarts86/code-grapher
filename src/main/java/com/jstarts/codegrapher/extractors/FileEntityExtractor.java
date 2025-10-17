package com.jstarts.codegrapher.extractors;

import java.nio.file.Paths;
import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.FileEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class FileEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        if (nodeType.equals("module")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<CodeEntity> extract(Node node, ExtractionContext context, String filePath, String sourceCode) {
        return buildFileEntity(node, context, filePath)
                .map(entity -> (CodeEntity) entity);
    }

    private Optional<FileEntity> buildFileEntity(Node node, ExtractionContext context, String filePath) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            SourceLocation location = buildLocation(filePath, node);
            FileEntity fileEntity = new FileEntity.Builder()
                    .name(fileName)
                    .moduleName(fileName)
                    .id(CodeEntity.generateId(location))
                    .location(location)
                    .parentId(null)
                    .type(CodeEntityType.FILE)
                    .build();
            return Optional.of(fileEntity);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    private SourceLocation buildLocation(String filePath, Node fileNode) {
        return SourceLocation.builder()
                .filePath(filePath)
                .startLine(fileNode.getStartPoint().getRow() + 1)
                .endLine(fileNode.getEndPoint().getRow() + 1)
                .startByte(fileNode.getStartByte())
                .endByte(fileNode.getEndByte())
                .startCol(fileNode.getStartPoint().getColumn())
                .endCol(fileNode.getEndPoint().getColumn())
                .build();
    }

}
