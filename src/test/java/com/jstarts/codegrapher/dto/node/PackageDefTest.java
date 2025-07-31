package com.jstarts.codegrapher.dto.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.parser.JavaParser;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class PackageDefTest {
    static {
        LibraryLoader.load();
    }

    public String filePath = "Test.java";
    public Parser parser = Parser.getFor(Language.JAVA);
    public JavaParser javaParser = new JavaParser(filePath, parser);

    @Test
    public void testExtractPackage() throws IOException {
        String code = Files.readString(Path.of(filePath));
        Tree tree = parser.parse(code);
        Node root = tree.getRootNode();
        PackageDef extractedPackage = javaParser.extractPackage(root, code);

        SourceLocation location = new SourceLocation("Test.java", 1, 1);
        PackageDef packageDef = new PackageDef("com.jstarts.test", location);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonExtractedPackage = gson.toJson(extractedPackage);
        String jsonPackageDef =gson.toJson(packageDef);
        assertEquals(jsonExtractedPackage, jsonPackageDef);
    }
}
