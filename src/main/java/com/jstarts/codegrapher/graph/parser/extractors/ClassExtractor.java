package com.jstarts.codegrapher.graph.parser.extractors;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.NodeDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.ClassDef;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;

import ch.usi.si.seart.treesitter.Node;

public class ClassExtractor implements CodeEntityExtractor {

    private final String packageName;

    public ClassExtractor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void extract(Node classNode, String sourceCode, String filePath) {
        // class_declaration
        Node nameNode = classNode.getChildByFieldName("name");

        Node modifiersNode = null;

        for (int i = 0; i < classNode.getChildCount(); i++) {
            if (classNode.getChild(i).getType().equals("modifiers")) {
                modifiersNode = classNode.getChild(i);
                break;
            }
        }
        if (nameNode != null) {
            String className = sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
            String modifiers = (modifiersNode != null) ? sourceCode.substring(modifiersNode.getStartByte(), modifiersNode.getEndByte()) : "";

            String fqn = packageName.isEmpty() ? className :  packageName + "." + className;

            SourceLocation location = SourceLocation.builder()
                .startLine(nameNode.getStartPoint().getRow() + 1)
                .filePath(filePath)
                .endLine(nameNode.getEndPoint().getRow() + 1)
                .startByte(nameNode.getStartByte())
                .endByte(nameNode.getEndByte())
                .startCol(nameNode.getStartPoint().getColumn())
                .endByte(nameNode.getEndPoint().getColumn())
                .build();
            ;
        }
    }
}
