package com.jstarts.codegrapher;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import ch.usi.si.seart.treesitter.printer.TreePrinter;

import com.falkordb.ResultSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.extractors.ClassEntityExtractor;
import com.jstarts.codegrapher.extractors.ExtractionContext;
import com.jstarts.codegrapher.extractors.ExtractorRegistry;
import com.jstarts.codegrapher.extractors.FileEntityExtractor;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    static {
        LibraryLoader.load();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        // Path path = Paths.get(new String(
        // Objects.requireNonNull(
        // ClassLoader.getSystemResourceAsStream("test/test.py").readAllBytes())));
        // String pythonSource = Files.readString(path);
        // Parser parser = Parser.getFor(Language.PYTHON);
        // Tree tree = parser.parse(pythonSource);

        var resource = Main.class.getResource("/test_files/class.py");
        String pythonSource = Files.readString(Path.of(resource.toURI()));
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(pythonSource);

        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());

        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(
                registry,
                context,
                "src/test/resources/test.py",
                pythonSource);

        walker.walk(tree.getRootNode());

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
