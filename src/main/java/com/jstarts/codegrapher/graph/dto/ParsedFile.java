package com.jstarts.codegrapher.graph.dto;

import com.jstarts.codegrapher.graph.dto.node.ImportDef;
import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.dto.node.TypeDef;

import java.util.List;

public class ParsedFile {

    private final String filePath;
    private final PackageDef packageDef;
    private final List<ImportDef> imports;
    private final List<TypeDef> topLevelTypes;

    public ParsedFile(String filePath, PackageDef packageDef, List<ImportDef> imports, List<TypeDef> topLevelTypes) {
        this.filePath = filePath;
        this.packageDef = packageDef;
        this.imports = imports;
        this.topLevelTypes = topLevelTypes;
    }

    // Getters for all fields

    public String getFilePath() {
        return filePath;
    }

    public PackageDef getPackageDef() {
        return packageDef;
    }

    public List<ImportDef> getImports() {
        return imports;
    }

    public List<TypeDef> getTopLevelTypes() {
        return topLevelTypes;
    }
}
