package com.jstarts.codegrapher.dto.node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.graph.dto.node.typedef.TypeDef;
import com.jstarts.codegrapher.graph.parser.JavaParser;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class TypeDefTest {
    static {
        LibraryLoader.load();

    }
    public String filePath = "Test.java";
    public Parser parser = Parser.getFor(Language.JAVA);
    public JavaParser javaParser = new JavaParser(filePath, parser);


    // @Test
    public void topLevelDefTest() throws IOException{
        String code = Files.readString(Path.of(filePath));
        Tree tree = parser.parse(code);
        Node root = tree.getRootNode(); 
        // List<TypeDef> extractedTypeDefs = javaParser.extractTypeDefs(root, code);

        // String code = Files.readString(Path.of(filePath));
        // Tree tree = p 



    }

}
