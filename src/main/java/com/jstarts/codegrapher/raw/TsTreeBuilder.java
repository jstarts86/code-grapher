package com.jstarts.codegrapher.raw;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;
import com.jstarts.codegrapher.raw.dto.RawSyntaxNode;

public class TsTreeBuilder {

    public RawSyntaxNode build(Tree tree, String sourceCode) {
        return buildRecursive(tree.getRootNode(), sourceCode);
    }

    private RawSyntaxNode buildRecursive(Node currentNode, String sourceCode) {
        RawSyntaxNode newNode = new RawSyntaxNode(currentNode, sourceCode);
        if(newNode.getType().contentEquals("class_declaration")){
            // System.out.println(newNode);
        }
        for (int i = 0; i < currentNode.getChildCount(); i++) {
            Node childTsNode = currentNode.getChild(i);
            RawSyntaxNode newChildNode = buildRecursive(childTsNode, sourceCode);
            newNode.addChild(newChildNode);
        }
        return newNode;
    }
}
