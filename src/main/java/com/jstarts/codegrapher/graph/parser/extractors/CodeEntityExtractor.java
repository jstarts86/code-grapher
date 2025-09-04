package com.jstarts.codegrapher.graph.parser.extractors;

import com.jstarts.codegrapher.graph.dto.node.NodeDef;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;
import ch.usi.si.seart.treesitter.Node;

public interface CodeEntityExtractor {
    void extract(Node node, String sourceCode, String filePath, String packageName, GraphBuilder graphBuilder);
}
