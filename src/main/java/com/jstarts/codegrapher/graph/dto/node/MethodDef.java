package com.jstarts.codegrapher.graph.dto.node;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDef extends NodeDef {

    private String name;
    private List<String> parameterList;
    private String type;
    private Boolean isStatic;
    private String accessModifier;
    public MethodDef(String fullyQualifiedName, SourceLocation sourceLocation, String name, List<String> parameterList,
            String type, Boolean isStatic, String accessModifier) {
        super(fullyQualifiedName, sourceLocation);
        this.name = name;
        this.parameterList = parameterList;
        this.type = type;
        this.isStatic = isStatic;
        this.accessModifier = accessModifier;
    }
}
