package com.jstarts.codegrapher.parsers;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;
import ch.usi.si.seart.treesitter.TreeCursor;
import ch.usi.si.seart.treesitter.TreeCursorNode;

public class ASTCursor implements AutoCloseable {
    private final TreeCursor cursor;

    public ASTCursor(TreeCursor cursor) {
        this.cursor = cursor;
    }

    public ASTCursor(Tree tree) {
        this.cursor = tree.getRootNode().walk();
    }

    public int getCurrentDepth() {
        return cursor.getCurrentDepth();
    }

    public Node getCurrentNode() {
        return cursor.getCurrentNode();
    }

    public String getCurrentFieldName() {
        return cursor.getCurrentFieldName();

    }

    public TreeCursorNode getCurrentTreeCursorNode() {
        return cursor.getCurrentTreeCursorNode();
    }

    public  boolean gotoFirstChild() {
        return cursor.gotoFirstChild();
    }

    public boolean gotoLastChild() {
        return cursor.gotoLastChild();
    }

    public boolean gotoNextSibling() {
        return cursor.gotoNextSibling();
    }


    public boolean gotoPrevSibling() {
        return cursor.gotoPrevSibling();
    }

    public boolean gotoParent() {
        return cursor.gotoParent();
    }

    public boolean gotoNode(Node node) {
        return cursor.gotoNode(node);
    }

    public boolean reset(TreeCursor other) {
        return cursor.reset(other);
    }
    public ASTCursor clone() {
        return new ASTCursor(cursor.clone());
    }

    @Override
    public void close() {
        cursor.close();
    }

     

    
}
