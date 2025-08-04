package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public class ClassDef extends TypeDef {

    public ClassDef(String name, String modifiers, SourceLocation sourceLocation, Boolean isMainType) {
        super(name, modifiers, sourceLocation, isMainType);
    }




}
