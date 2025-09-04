package com.jstarts.codegrapher.graph.dto.edge;

import com.jstarts.codegrapher.graph.dto.node.NodeDef;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Edge {
    private NodeDef fromNodeFQN;
    private NodeDef toNodeFQN;
    EdgeType type;
}
