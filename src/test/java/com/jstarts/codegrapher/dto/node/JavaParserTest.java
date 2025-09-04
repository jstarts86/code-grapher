package com.jstarts.codegrapher.dto.node;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;


public class JavaParserTest {
    static {
        LibraryLoader.load();
    }

    public String filePath = "Test.java";
    public Parser parser = Parser.getFor(Language.JAVA);
}
