package com.jstarts.codegrapher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.examples.BasicExample;
import com.jstarts.codegrapher.examples.TreeCursorExample;
import com.jstarts.codegrapher.examples.TreeQueryExample;
import com.jstarts.codegrapher.graph.dto.ParsedFile;
import com.jstarts.codegrapher.graph.dto.node.typedef.ClassDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.TypeDef;
import com.jstarts.codegrapher.graph.parser.JavaParser;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class Main {
    static {
        LibraryLoader.load();
    }

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        // System.out.println("Attempting to use java-tree-sitter ");
        // BasicExample example1 = new BasicExample();
        // TreeCursorExample example2 = new TreeCursorExample();
        // TreeQueryExample example3 = new TreeQueryExample();
        // System.err.println("DEBUGPRINT[17]: Main.java:15: example2=" + example2);

        // example1.run();
        // example2.run();
        // example3.run();
        System.out.println("Running Parser Test");

        String filePath = "Test.java";

        try (Parser treeSitterParser = Parser.getFor(Language.JAVA)) {

            String code = Files.readString(Path.of(filePath));
            Tree tree = treeSitterParser.parse(code);
            Node root = tree.getRootNode();
            // JavaParser myParser = new JavaParser(filePath, treeSitterParser);
            List<ClassDef> typeDefs = myParser.extractClass(root, code);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(typeDefs);
            System.out.println("parser output");
            System.out.println(jsonOutput);

        } catch (IOException er) {
            System.err.println("Error reading or parsing file: " + er.getMessage());
            er.printStackTrace();
        }

    }

}
