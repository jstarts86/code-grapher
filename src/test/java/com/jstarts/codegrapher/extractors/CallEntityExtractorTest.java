package com.jstarts.codegrapher.extractors;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class CallEntityExtractorTest {

    static {
        LibraryLoader.load();
    }

    @Test
    void runCallExtractorManually() throws Exception {
        CallEntityExtractorTest.main(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        // 1. Load sample Python file
        Path path = Path.of("src/test/resources/test_files/quick_call_test.py");
        System.out.println("[DEBUG] Absolute path: " + path.toAbsolutePath());
        System.out.println("[DEBUG] Exists: " + Files.exists(path));

        String sourceCode = Files.readString(path);

        // 2. Parse code with Tree-sitter
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(sourceCode);

        // 3. Set up registry with Function/Class/File + Call extractors
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("module", new FileEntityExtractor());
        registry.register("call", new CallEntityExtractor());

        // 4. Traverse AST
        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker =
                new PythonTreeWalker(registry, context, path.toString(), sourceCode);

        System.out.println("[DEBUG] Source:\n" + sourceCode);
        walker.walk(tree.getRootNode());

        // 5. Dump extracted entities for manual inspection
        System.out.println("\n=== Extracted Entities ===");
        context.getAllEntities().forEach(System.out::println);

        // 6. Assertions: ensure at least one CallEntity found
        List<CodeEntity> calls = context.getAllEntities().stream()
                .filter(e -> e.getType() == CodeEntityType.CALL)
                .toList();

        assertFalse(calls.isEmpty(), "Expected at least one CallEntity");

        // Optional: verify specific callees exist
        boolean hasPrint =
                calls.stream().anyMatch(e -> e.getName().contains("print"));
        boolean hasSum =
                calls.stream().anyMatch(e -> e.getName().contains("sum"));
        boolean hasGreet =
                calls.stream().anyMatch(e -> e.getName().contains("greet"));

        assertTrue(hasPrint, "Expected to detect call to print()");
        assertTrue(hasSum, "Expected to detect call to sum()");
        assertTrue(hasGreet, "Expected to detect call to greet()");
    }
}
