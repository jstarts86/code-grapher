package com.jstarts.codegrapher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.extractors.ClassEntityExtractor;
import com.jstarts.codegrapher.extractors.ExtractionContext;
import com.jstarts.codegrapher.extractors.ExtractorRegistry;
import com.jstarts.codegrapher.extractors.FileEntityExtractor;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class Main {
    static {
        LibraryLoader.load();
    }

    public static void main(String[] args) throws Exception {
        // 1. Setup Repository Path
        // Use the sample project for demonstration
        Path repoPath = Path.of("src/test/resources/test_repos/sample_project").toAbsolutePath();
        if (!Files.exists(repoPath)) {
            System.err.println("Sample project not found at " + repoPath);
            return;
        }

        System.out.println("Processing repository: " + repoPath);

        // 2. Configure Extractors
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("function_definition", new com.jstarts.codegrapher.extractors.FunctionEntityExtractor());
        registry.register("import_statement", new com.jstarts.codegrapher.extractors.ImportEntityExtractor());
        registry.register("import_from_statement", new com.jstarts.codegrapher.extractors.ImportEntityExtractor());
        registry.register("call", new com.jstarts.codegrapher.extractors.CallEntityExtractor());
        registry.register("assignment", new com.jstarts.codegrapher.extractors.VariableEntityExtractor());
        registry.register("typed_assignment", new com.jstarts.codegrapher.extractors.VariableEntityExtractor());

        // 3. Run Extraction
        com.jstarts.codegrapher.core.RepositoryProcessor processor = new com.jstarts.codegrapher.core.RepositoryProcessor(
                registry);
        ExtractionContext context = processor.process(repoPath);
        List<CodeEntity> entities = context.getAllEntities();
        System.out.println("Extracted " + entities.size() + " entities.");

        // 4. Build Symbol Table
        com.jstarts.codegrapher.core.GlobalSymbolTable symbolTable = new com.jstarts.codegrapher.core.GlobalSymbolTable(
                entities, repoPath);

        // 5. Resolve Imports
        com.jstarts.codegrapher.core.ImportResolver importResolver = new com.jstarts.codegrapher.core.ImportResolver(
                symbolTable);
        importResolver.resolveImports(entities);
        System.out.println("Imports resolved.");

        // 6. Resolve Calls
        com.jstarts.codegrapher.core.CallGraphResolver callResolver = new com.jstarts.codegrapher.core.CallGraphResolver(
                symbolTable);
        callResolver.resolveCalls(entities);
        System.out.println("Calls resolved.");

        // 7. Persist to Database
        try {
            com.jstarts.codegrapher.db.FalkorDBClient.use("localhost", 6379, "CodeGraph", client -> {
                com.jstarts.codegrapher.db.GraphPersister persister = new com.jstarts.codegrapher.db.GraphPersister(
                        client);
                persister.persist(entities);
                System.out.println("Graph persisted to FalkorDB 'CodeGraph'.");
                return null;
            });
        } catch (Exception e) {
            System.err.println(
                    "Failed to persist to database. Is FalkorDB running? (docker run -p 6379:6379 -it --rm falkordb/falkordb:edge)");
            e.printStackTrace();
        }

        printExtractedEntities(context);
    }

    private static void printExtractedEntities(ExtractionContext context) {
        List<CodeEntity> entities = context.getAllExtractedEntities();

        // Group by parent
        Map<String, List<CodeEntity>> byParent = entities.stream()
                .collect(Collectors.groupingBy(e -> e.getParentId() != null ? e.getParentId() : "ROOT"));

        // Print tree
        printTree(byParent, "ROOT", 0);
    }

    private static void printTree(Map<String, List<CodeEntity>> byParent, String parentId, int depth) {
        List<CodeEntity> children = byParent.getOrDefault(parentId, List.of());

        for (CodeEntity child : children) {
            // System.out.println(" ".repeat(depth) + child.getType() + ": " +
            // child.getName());
            System.out.println(child);
            printTree(byParent, child.getId(), depth + 1);
        }
    }

}
