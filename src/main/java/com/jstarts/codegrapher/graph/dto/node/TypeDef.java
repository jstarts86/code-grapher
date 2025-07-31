package com.jstarts.codegrapher.graph.dto.node;

import java.util.List;

import com.jstarts.codegrapher.graph.dto.metadata.JavaModifier;
import com.jstarts.codegrapher.graph.dto.metadata.Scope;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public abstract class TypeDef {
    private List<JavaModifier> javaModifiers;
    private Scope scope;
    private SourceLocation sourceLocation;
    private Boolean isMainType;
    public List<JavaModifier> getJavaModifiers() {
        return javaModifiers;
    }
    public void setJavaModifiers(List<JavaModifier> javaModifiers) {
        this.javaModifiers = javaModifiers;
    }
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }
    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }
    public Boolean getIsMainType() {
        return isMainType;
    }
    public void setIsMainType(Boolean isMainType) {
        this.isMainType = isMainType;
    }
}
