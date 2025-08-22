package com.jstarts.codegrapher.graph.dto.node;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class NodeDef {
    private String fullyQualifiedName;
    private SourceLocation sourceLocation;
}
