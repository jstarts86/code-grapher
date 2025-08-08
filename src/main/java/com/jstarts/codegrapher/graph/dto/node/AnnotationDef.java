package com.jstarts.codegrapher.graph.dto.node;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnnotationDef {
    private String typeName;
    private String annotationName;
    private List<String> arguments;
    
}
