package com.jstarts.codegrapher;

import com.jstarts.codegrapher.examples.BasicExample;
import com.jstarts.codegrapher.examples.TreeCursorExample;
import ch.usi.si.seart.treesitter.*;

public class Main {
    static {
        LibraryLoader.load();
    }
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        System.out.println("Attempting to use java-tree-sitter ");
        BasicExample example1 = new BasicExample();
        TreeCursorExample example2 = new TreeCursorExample();
        System.err.println("DEBUGPRINT[17]: Main.java:15: example2=" + example2);

        example1.run();
        example2.run();

    }


}
