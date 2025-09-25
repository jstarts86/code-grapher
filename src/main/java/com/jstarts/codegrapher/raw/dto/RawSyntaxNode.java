package com.jstarts.codegrapher.raw.dto;

import java.util.ArrayList;
import java.util.List;

import ch.usi.si.seart.treesitter.Node;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RawSyntaxNode {
    private final String type;
    private final String text;
    private final int startByte;
    private final int endByte;
    private final int startLine;
    private final int startCol;
    private final int endLine;
    private final int endCol;
    private final List<RawSyntaxNode> children;
    private transient RawSyntaxNode parent;

    public RawSyntaxNode(Node tsNode, String sourceCode) {
        this.type = tsNode.getType();
        this.text = sourceCode.substring(tsNode.getStartByte(), tsNode.getEndByte());
        this.startByte = tsNode.getStartByte();
        this.endByte = tsNode.getEndByte();
        this.startLine = tsNode.getStartPoint().getRow() + 1;
        this.startCol = tsNode.getStartPoint().getColumn();
        this.endLine = tsNode.getEndPoint().getRow() + 1;
        this.endCol = tsNode.getEndPoint().getColumn();
        this.children = new ArrayList<>();
    }

    public void addChild(RawSyntaxNode child) {
        this.children.add(child);
        child.setParent(this);
    }

}
