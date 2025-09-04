package com.jstarts.codegrapher;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;
import com.jstarts.codegrapher.graph.parser.SingleFileTreeWalker;
import com.jstarts.codegrapher.graph.parser.extractors.AnnotationExtractor;
import com.jstarts.codegrapher.graph.parser.extractors.ClassExtractor;
import com.jstarts.codegrapher.raw.TsTreeBuilder;
import com.jstarts.codegrapher.raw.dto.TsNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static {
        LibraryLoader.load();
    }

    public static void testRawParse(Parser treeSitterParser, String code) {
        Tree tree = treeSitterParser.parse(code);
        TsTreeBuilder builder = new TsTreeBuilder();
        TsNode rawRoot = builder.build(tree, code);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(rawRoot);
        System.out.println("Raw TsNode Tree");
        System.out.println(jsonOutput);
        System.out.println(" End of Raw TsNode Tree ");
    }

    public static void testExtractor(String code, String filePath) {
        // GraphBuilder graphBuilder = new GraphBuilder("Test.java", nodes, lastAddedNode)
        SingleFileTreeWalker treeWalker = new SingleFileTreeWalker(code,filePath);
        treeWalker.register("class_declaration", new ClassExtractor());
        treeWalker.register("annotation", new AnnotationExtractor());
        treeWalker.setRootNode(code);
        treeWalker.setPackageName(treeWalker.findPackageName(code));
        treeWalker.walk(treeWalker.getRoot());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String jsonOutput = gson.toJson(treeWalker.getGraphBuilder().getNodes());
        System.out.println("Extracted nodes:");
        System.out.println(jsonOutput);


    }
    public static void main(String[] args) {
        // System.out.println("Running Raw Tree-sitter Builder Test");

        String filePath = "Test.java";

        try (Parser treeSitterParser = Parser.getFor(Language.JAVA)) {

            String code = Files.readString(Path.of(filePath));
            // testExtractor(code, filePath);
            testRawParse(treeSitterParser, code);
            // String testRaw = testRawParse(treeSitterParser,code);
            // System.out.println("Raw TsNode Tree");
            // System.out.println(testRaw);
            // System.out.println(" End of Raw TsNode Tree ");

            // FalkorConfig.main(new String[0]);

        } catch (IOException er) {
            System.err.println("Error reading or parsing file: " + er.getMessage());
            er.printStackTrace();
        }
    }
}
