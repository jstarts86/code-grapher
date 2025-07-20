package com.jstarts.codegrapher.graph.dto.node;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public class ImportDef {
    private String name;
    private String fullyQualifiedName;
    private TypeKind type;
    private SourceLocation location;

    @Override
    public String toString() {
        return super.toString();
    }

}
