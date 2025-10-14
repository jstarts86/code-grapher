package com.jstarts.codegrapher.core.entities;


public record SourceLocation (
    String filePath,
    int startLine,
    int endLine,
    int startCol,
    int endCol,
    int startByte,
    int endByte
) {}
