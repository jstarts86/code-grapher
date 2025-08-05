package com.jstarts.codegrapher.graph.dto.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldDef {

    private String name;
    private String fullyQualifiedName;
    private String type;
    private boolean isStatic;
    private String accessModifier;

}
