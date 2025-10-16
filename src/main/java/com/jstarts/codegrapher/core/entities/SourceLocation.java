package com.jstarts.codegrapher.core.entities;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
public record SourceLocation(
        String filePath,
        int startLine,
        int endLine,
        int startCol,
        int endCol,
        int startByte,
        int endByte) {
}
