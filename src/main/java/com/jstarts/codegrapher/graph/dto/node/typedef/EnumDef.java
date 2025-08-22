package com.jstarts.codegrapher.graph.dto.node.typedef;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnumDef extends TypeDef {
    public EnumDef(String name, String modifiers, SourceLocation sourceLocation, String fullyQualifiedName) {
        super(name, modifiers, sourceLocation, fullyQualifiedName);
    }
}
