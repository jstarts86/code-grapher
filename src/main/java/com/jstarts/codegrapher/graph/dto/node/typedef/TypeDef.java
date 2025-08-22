package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.NodeDef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TypeDef extends NodeDef {
    private String name;
    private String modifiers;
    public TypeDef(String fullyQualifiedName, SourceLocation sourceLocation, String name, String modifiers) {
        super(fullyQualifiedName, sourceLocation);
        this.name = name;
        this.modifiers = modifiers;
    }



}
