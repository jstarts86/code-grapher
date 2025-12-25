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
        if (args.length == 0) {
            printUsage();
            return;
        }

        String repoPathStr = null;
        String dbHost = "localhost";
        int dbPort = 6379;
        String graphKey = "CodeGraph";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--repo":
                    if (i + 1 < args.length)
                        repoPathStr = args[++i];
                    break;
                case "--host":
                    if (i + 1 < args.length)
                        dbHost = args[++i];
                    break;
                case "--port":
                    if (i + 1 < args.length)
                        dbPort = Integer.parseInt(args[++i]);
                    break;
                case "--graph":
                    if (i + 1 < args.length)
                        graphKey = args[++i];
                    break;
                case "--help":
                    printUsage();
                    return;
                default:
                    // Assume first arg is repo if not flagged, or error
                    if (repoPathStr == null && !args[i].startsWith("-")) {
                        repoPathStr = args[i];
                    } else {
                        System.err.println("Unknown argument: " + args[i]);
                        printUsage();
                        return;
                    }
            }
        }

        if (repoPathStr == null) {
            System.err.println("Error: Repository path is required.");
            printUsage();
            return;
        }

        Path repoPath = Path.of(repoPathStr).toAbsolutePath();
        if (!Files.exists(repoPath)) {
            System.err.println("Error: Repository not found at " + repoPath);
            return;
        }

        System.out.println("Processing repository: " + repoPath);
        System.out.println("Database: " + dbHost + ":" + dbPort + " (" + graphKey + ")");

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
        String finalDbHost = dbHost;
        int finalDbPort = dbPort;
        String finalGraphKey = graphKey;
        try {
            com.jstarts.codegrapher.db.FalkorDBClient.use(dbHost, dbPort, graphKey, client -> {
                com.jstarts.codegrapher.db.GraphPersister persister = new com.jstarts.codegrapher.db.GraphPersister(
                        client);
                persister.persist(entities);
                System.out.println("Graph persisted to FalkorDB '" + finalGraphKey + "'.");
                return null;
            });
        } catch (Exception e) {
            System.err.println(
                    "Failed to persist to database. Is FalkorDB running at " + finalDbHost + ":" + finalDbPort + "?");
            e.printStackTrace();
        }

        // printExtractedEntities(context);
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar code-grapher.jar --repo <path> [options]");
        System.out.println("Options:");
        System.out.println("  --repo <path>   Path to the repository to process");
        System.out.println("  --host <host>   FalkorDB host (default: localhost)");
        System.out.println("  --port <port>   FalkorDB port (default: 6379)");
        System.out.println("  --graph <key>   Graph key name (default: CodeGraph)");
        System.out.println("  --help          Show this help message");
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
