package com.jstarts.codegrapher;

import ch.usi.si.seart.treesitter.*;

public class Main {
    static {
        LibraryLoader.load();
    }
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        System.out.println("Attempting to use java-tree-sitter ");

        try (
            Parser parser = Parser.getFor(Language.PYTHON);
            Tree tree = parser.parse("def foo(bar, baz):\n print(bar)\n print(baz)");
        ) {
            Node root = tree.getRootNode();
            assert root.getChildCount() ==1;
            System.out.println(root.getType());
            assert root.getType().equals("module");
            assert root.getStartByte() == 0;
            assert root.getEndByte() == 44;
            Node function = root.getChild(0);
            assert function.getType().equals("function_definition");
            assert function.getChildCount() == 5;
        } catch (Exception ex) {
            System.out.println("hello");


        }
    }


}
