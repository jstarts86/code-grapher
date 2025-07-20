package com.jstarts.codegrapher.graph.dto.metadata;

public class SourceLocation {
    private String filePath;
    private int startLine;
    private int endLine;
    private int startChar;
    private int endChar;

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    public SourceLocation(String filePath, int startLine, int endLine) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
    }
}
