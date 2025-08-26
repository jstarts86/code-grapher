package com.jstarts.codegrapher.graph.dto;

// import com.jstarts.codegrapher.graph.dto.node.ImportDef;
// import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.TypeDef;
// import com.jstarts.codegrapher.graph.dto.usage.TypeUsage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ParsedFile {
    private String filePath;
    // private PackageDef packageDef;
    // private List<ImportDef> imports;
    private List<TypeDef> types;
    // private List<TypeUsage> typeUsages;
    private List<Object> edges; // Assuming this might be used later
}
