package com.jstarts.codegrapher.graph.dto.metadata;

public class SourceLocation {
    private String filePath;
    private int startLine;
    private int endLine;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "filePath: " + this.getFilePath() + "\n" + "Start line: " + this.getStartLine() + "\n End line: "
                + this.getEndLine();
    }

    public SourceLocation(String filePath, int startLine, int endLine) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
    }
}
