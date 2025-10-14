package com.jstarts.codegrapher;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import ch.usi.si.seart.treesitter.printer.TreePrinter;

import com.falkordb.ResultSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.falkordb.ingestors.RawSyntaxNodeIngestor;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;
import com.jstarts.codegrapher.graph.parser.SingleFileTreeWalker;
import com.jstarts.codegrapher.graph.parser.extractors.AnnotationExtractor;
import com.jstarts.codegrapher.graph.parser.extractors.ClassExtractor;
import com.jstarts.codegrapher.raw.TsTreeBuilder;
import com.jstarts.codegrapher.raw.dto.RawSyntaxNode;

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
        RawSyntaxNode rawRoot = builder.build(tree, code);
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
    public static RawSyntaxNode rawParse(Parser treeSitterParser, String code) {
        Tree tree = treeSitterParser.parse(code);
        TsTreeBuilder builder = new TsTreeBuilder();
        RawSyntaxNode rawRoot = builder.build(tree, code);
        return rawRoot;
    }

    public static void main(String[] args) {
        try {
            FalkorConfig config = new FalkorConfig("localhost", 6379, "rawRepoLayer");
            String projectRoot = "test-data/gson";
            // String projectRoot = "."; 
            JavaRepoRawNodeIngestor repoIngestor = new JavaRepoRawNodeIngestor(config, projectRoot);
            
            System.out.println("clear graph");
            config.executeQuery("MATCH (n) DETACH DELETE n");

            System.out.println("repository ingestion start");
            repoIngestor.ingestRepository();
            System.out.println("repo ingestion finish");

        } catch (IOException er) {
            System.err.println("Error reading or parsing file: " + er.getMessage());
            er.printStackTrace();
        }
    }
}
