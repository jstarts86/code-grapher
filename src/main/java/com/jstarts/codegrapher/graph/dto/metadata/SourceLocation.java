package com.jstarts.codegrapher.graph.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SourceLocation {
    private String filePath;
    private int startLine;
    private int endLine;
    private int startCol;
    private int endCol;
    private int startByte;
    private int endByte;
}
