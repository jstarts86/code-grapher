package com.jstarts.codegrapher.dto.node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class TypeDefTest {
    public String filePath = "Test.java";
    public Parser parser = Parser.getFor(Language.JAVA);


    // @Test
    public void topLevelDefTest() throws IOException{
        // String code = Files.readString(Path.of(filePath));
        // Tree tree = p 



    }

}
