package com.jstarts.codegrapher.extractors;

import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;

import ch.usi.si.seart.treesitter.Node;

public interface EntityExtractor {
    boolean canHandle(String nodeType);

    // private Optional<CodeEntity> extract(Node node, )

}
