package com.jstarts.codegrapher.examples;
import ch.usi.si.seart.treesitter.*;


public class TreeCursorExample {
    public void run() {
        String type;
        try (
            Parser parser = Parser.getFor(Language.PYTHON);
            Tree tree = parser.parse("def foo(bar, baz):\n  print(bar)\n  print(baz)");
            TreeCursor cursor = tree.getRootNode().walk()
        ) {
            type = cursor.getCurrentTreeCursorNode().getType();
            assert type.equals("module");
            cursor.gotoFirstChild();
            type = cursor.getCurrentTreeCursorNode().getType();
            assert type.equals("function_definition");
            cursor.gotoFirstChild();
            type = cursor.getCurrentTreeCursorNode().getType();
            assert type.equals("def");
            cursor.gotoNextSibling();
            cursor.gotoParent();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
