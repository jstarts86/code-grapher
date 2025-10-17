package com.jstarts.codegrapher.extractors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class GraphExtractionTest {
    static {
        LibraryLoader.load();
    }

    public static void main(String[] args) throws IOException {
        // 1. Load test file

        String pythonSource;
        try (var stream = ClassLoader.getSystemResourceAsStream("test_files/class.py")) {
            pythonSource = new String(
                    Objects.requireNonNull(stream).readAllBytes(),
                    StandardCharsets.UTF_8);
        }
        // 2. Parse with Tree-sitter
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(pythonSource);

        // 3. Set up extractors
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_declaration", new ClassEntityExtractor());

        // 4. Walk the tree
        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(
                registry,
                context,
                "src/test/resources/test.py",
                pythonSource);

        walker.walk(tree.getRootNode());

        // 5. Print results
        printExtractedEntities(context);
    }

    private static void printExtractedEntities(ExtractionContext context) {
        // Print all extracted entities in a tree format
        while (context.getContextStack() != null) {
            context.popContext();
        }
    }

}
