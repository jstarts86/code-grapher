package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

import lombok.AllArgsConstructor;



public class ClassDef extends TypeDef {

    public ClassDef(String name, String modifiers, SourceLocation sourceLocation, String fullyQualifiedName) {
        super(name, modifiers, sourceLocation, fullyQualifiedName);
    }






}
