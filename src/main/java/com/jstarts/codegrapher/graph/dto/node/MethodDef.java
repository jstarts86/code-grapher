package com.jstarts.codegrapher.graph.dto.node;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MethodDef {

    private String name;
    private String fullyQualifiedName;
    private List<String> parameterList;
    private String type;
    private Boolean isStatic;
    private String accessModifier;
}
