package com.jstarts.codegrapher.graph.dto.node;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public class PackageDef {

    private String fullyQualifiedName;
    private SourceLocation location;



    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public void setLocation(SourceLocation location) {
        this.location = location;
    }

    public PackageDef(String fullyQualifiedName, SourceLocation location) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.location = location;
    }

    @Override
    public String toString() {
        return "Name:\n" + this.getFullyQualifiedName() + "\n" + "Source Location:\n" + this.getLocation();
    }

}
