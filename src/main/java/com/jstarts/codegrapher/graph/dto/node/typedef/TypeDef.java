package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.JavaModifier;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public abstract class TypeDef {
    private String name;
    private String modifiers;
    private SourceLocation sourceLocation;
    private Boolean isMainType;
    public TypeDef(String name, String modifiers, SourceLocation sourceLocation, Boolean isMainType) {
        this.name = name;
        this.modifiers = modifiers;
        this.sourceLocation = sourceLocation;
        this.isMainType = isMainType;
    }


}
