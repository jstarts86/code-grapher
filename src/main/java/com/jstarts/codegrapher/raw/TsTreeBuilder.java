package com.jstarts.codegrapher.raw;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;
import com.jstarts.codegrapher.raw.dto.TsNode;

public class TsTreeBuilder {

    public TsNode build(Tree tree, String sourceCode) {
        return buildRecursive(tree.getRootNode(), sourceCode);
    }

    private TsNode buildRecursive(Node currentNode, String sourceCode) {
        TsNode newNode = new TsNode(currentNode, sourceCode);
        if(newNode.getType().contentEquals("class_declaration")){
            // System.out.println(newNode);
        }
        for (int i = 0; i < currentNode.getChildCount(); i++) {
            Node childTsNode = currentNode.getChild(i);
            TsNode newChildNode = buildRecursive(childTsNode, sourceCode);
            newNode.addChild(newChildNode);
        }
        return newNode;
    }
}
