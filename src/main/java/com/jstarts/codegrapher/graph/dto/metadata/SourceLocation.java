package com.jstarts.codegrapher.graph.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SourceLocation {
    private String filePath;
    private int startLine;
    private int endLine;

}
