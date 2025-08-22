package com.jstarts.codegrapher.graph.parser;

import com.jstarts.codegrapher.graph.dto.edge.Edge;
import com.jstarts.codegrapher.graph.dto.edge.EdgeType;
import com.jstarts.codegrapher.graph.dto.node.NodeDef;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GraphBuilder {

    private String filePath;
    private Map<String, NodeDef> nodes = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private NodeDef lastAddedNode;

    public void registerNode(NodeDef node) {
        nodes.put(node.getFullyQualifiedName(), node);
        this.lastAddedNode = node;
    }

    public void addEdge(NodeDef from, NodeDef to, EdgeType type) {
        edges.add(new Edge(from, to, type));
    }
}
