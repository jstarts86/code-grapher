package com.jstarts.codegrapher.graph.parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jstarts.codegrapher.graph.dto.node.NodeDef;
import com.jstarts.codegrapher.graph.parser.extractors.CodeEntityExtractor;

import ch.usi.si.seart.treesitter.Node;

public class TreeWalker {

    private final Map<String, List<CodeEntityExtractor>> registry = new HashMap<>();
    private final String sourceCode;
    private final GraphBuilder graphBuilder;
    private final Deque<NodeDef> contextStack = new LinkedList<>();
    public TreeWalker(String sourceCode, GraphBuilder graphBuilder) {
        this.sourceCode = sourceCode;
        this.graphBuilder = graphBuilder;
    }

    public void register(String nodeType, CodeEntityExtractor extractor) {
        this.registry.computeIfAbsent(nodeType, k -> new ArrayList<>()).add(extractor);
    }

    public void walk(Node node) {
        List<CodeEntityExtractor> applicableExtractors = registry.get(node.getType());
        if (applicableExtractors != null) {
            for (CodeEntityExtractor extractor : applicableExtractors) {
                extractor.extract(node, this.sourceCode, this.graphBuilder, contextStack.peek());
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i));
        }
    }
    
}
