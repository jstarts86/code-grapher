package com.jstarts.codegrapher.graph.parser.extractors;

import java.lang.reflect.AccessFlag.Location;

import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.typedef.ClassDeclarationDef;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;

import ch.usi.si.seart.treesitter.Node;

public class ClassExtractor implements CodeEntityExtractor {

    /*
     * (class_declaration (modifiers)* @classModifier name: (identifier) @className)
     *
     */

    @Override
    public void extract(Node classNode, String sourceCode, String filePath, String packageName,
            GraphBuilder graphBuilder) {
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
            String modifiers = (modifiersNode != null)
                    ? sourceCode.substring(modifiersNode.getStartByte(), modifiersNode.getEndByte())
                    : "";

            String fqn = packageName.isEmpty() ? className : packageName + "." + className;

            SourceLocation location = SourceLocation.builder()
                    .filePath(filePath)
                    .startLine(nameNode.getStartPoint().getRow() + 1)
                    .endLine(nameNode.getEndPoint().getRow() + 1)
                    .startByte(nameNode.getStartByte())
                    .endByte(nameNode.getEndByte())
                    .startCol(nameNode.getStartPoint().getColumn())
                    .endCol(nameNode.getEndPoint().getColumn())
                    .build();
            ;
            ClassDeclarationDef classDef = new ClassDeclarationDef(fqn, location, className, modifiers);
            graphBuilder.registerNode(classDef);
        }
    }
}
