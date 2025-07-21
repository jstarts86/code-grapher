package com.jstarts.codegrapher;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.examples.BasicExample;
import com.jstarts.codegrapher.examples.TreeCursorExample;
import com.jstarts.codegrapher.examples.TreeQueryExample;
import com.jstarts.codegrapher.graph.dto.ParsedFile;
import com.jstarts.codegrapher.graph.parser.JavaParser;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;

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
            JavaParser myParser = new JavaParser(filePath, treeSitterParser);
            ParsedFile parsedFile = myParser.parse();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(parsedFile);
            System.out.println("parser output");
            System.out.println(jsonOutput);

        } catch (IOException er) {
            System.err.println("Error reading or parsing file: " + er.getMessage());
            er.printStackTrace();
        }

    }

}
