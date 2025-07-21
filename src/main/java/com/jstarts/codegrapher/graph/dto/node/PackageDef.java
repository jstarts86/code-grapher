package com.jstarts.codegrapher.graph.dto.node;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;

public class PackageDef {

    private String name;
    private SourceLocation location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public void setLocation(SourceLocation location) {
        this.location = location;
    }

    public PackageDef(String name, SourceLocation location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public String toString() {
        return "Name:\n" + this.getName() + "\n" + "Source Location:\n" + this.getLocation();
    }

}
