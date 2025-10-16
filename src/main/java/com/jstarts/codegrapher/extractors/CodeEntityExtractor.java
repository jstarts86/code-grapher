package com.jstarts.codegrapher.extractors;

import java.util.Optional;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import ch.usi.si.seart.treesitter.Node;

public interface CodeEntityExtractor {
    boolean canHandle(String nodeType);

    Optional<CodeEntity> extract(Node node, ExtractionContext context, String sourceFilePath, String sourceCode);


}
