package com.jstarts.codegrapher.extractors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class FunctionEntityExtractorTest {
    static {
        LibraryLoader.load();
    }

    @Test
    void runFunctionExtractorManually() throws Exception {
        FunctionEntityExtractorTest.main(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        // 1. Load test file
        Path path = Path.of("src/test/resources/test_files/function.py");
        System.out.println("[DEBUG] Absolute path: " + path.toAbsolutePath());
        System.out.println("[DEBUG] Exists: " + Files.exists(path));
        String sourceCode = Files.readString(path);

        // 2. Parse code
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(sourceCode);

        // 3. Prepare extractors
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("module", new FileEntityExtractor());

        // 4. Create context + walker
        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(
                registry,
                context,
                path.toString(),
                sourceCode);

        // 5. Walk AST
        System.out.print(sourceCode);
        walker.walk(tree.getRootNode());

        // 6. Print results
        context.getAllEntities().forEach(entity -> {
            System.out.printf(
                    "%s | name=%s | parent=%s | loc=[%d-%d]%n",
                    entity.getType(),
                    entity.getName(),
                    entity.getParentId(),
                    entity.getLocation().startLine(),
                    entity.getLocation().endLine());
        });
    }
}
