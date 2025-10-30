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

    void runFunctionExtractorManually() throws Exception {
        GraphExtractionTest.main(new String[0]);
    }

    public static void main(String[] args) throws IOException {
        // 1. Load test file

        // 2. Parse with Tree-sitter
        Path path = Path.of("src/test/resources/test_files/function.py");
        String pythonSource = Files.readString(path);
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(pythonSource);

        // 3. Set up extractors
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_declaration", new ClassEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());

        // 4. Walk the tree
        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(
                registry,
                context,
                "src/test/resources/test_files/function.py",
                pythonSource);

        walker.walk(tree.getRootNode());
        System.out.print(walker.getContext().getAllEntities());

        // 5. Print results
        // printExtractedEntities(context);
    }

    private static void printExtractedEntities(ExtractionContext context) {
        // Print all extracted entities in a tree format
        while (context.getContextStack() != null) {
            context.popContext();
        }
    }

}
