package com.jstarts.codegrapher.graph.dto.node.typedef;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.JavaModifier;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class TypeDef {
    private String name;
    private String modifiers;
    private SourceLocation sourceLocation;
    private Boolean isMainType;


}
