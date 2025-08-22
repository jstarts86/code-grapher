package com.jstarts.codegrapher.graph.dto.usage;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TypeUsage {
    private String name;
    private SourceLocation sourceLocation;
    private String fullyQualifiedName;
    private TypeUseKind typeUseKind;
}

enum TypeUseKind {
    TYPE_IDENTIFIER,
    PRIMITIVE,
    ARRAY,
    
}
