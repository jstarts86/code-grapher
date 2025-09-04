package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

import lombok.AllArgsConstructor;



public class ClassDeclarationDef extends TypeDef {

    public ClassDeclarationDef(String fullyQualifiedName, SourceLocation sourceLocation, String name, String modifiers) {
        super(fullyQualifiedName, sourceLocation, name, modifiers);
    }







}
