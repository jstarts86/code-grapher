package com.jstarts.codegrapher.examples;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Query;
import ch.usi.si.seart.treesitter.QueryCursor;
import ch.usi.si.seart.treesitter.QueryMatch;
import ch.usi.si.seart.treesitter.Tree;

public class TreeQueryExample {
    public void run(){
        Language language = Language.PYTHON;
        Query query = Query.getFor(language, "(identifier) @target");
        Parser parser = Parser.getFor(language);
        Tree tree = parser.parse("def foo(bar, baz):\n  print(bar)\n  print(baz)");
        Node root = tree.getRootNode();
        QueryCursor cursor = root.walk(query);
        int count = 0;
        for (QueryMatch match: cursor){
            System.err.println("DEBUGPRINT[50]: TreeQueryExample.java:18: match=" + match);
            count++;
        }

        assert count == 7;
    }


}
