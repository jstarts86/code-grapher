package com.jstarts.codegrapher.extractors;

import java.util.List;

import com.jstarts.codegrapher.core.entities.CodeEntity;

import ch.usi.si.seart.treesitter.Node;

public interface MultiEntityExtractor extends CodeEntityExtractor {
    List<CodeEntity> extractMany(Node node, ExtractionContext context, String filePath, String sourceCode);
}
