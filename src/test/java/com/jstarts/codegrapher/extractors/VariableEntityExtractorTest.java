package com.jstarts.codegrapher.extractors;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.VariableEntity;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class VariableEntityExtractorTest {
    static {
        LibraryLoader.load();
    }

    @Test
    void runVariableExtractorManually() throws Exception {
        VariableEntityExtractorTest.main(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        // 1. Load sample Python file
        Path path = Path.of("src/test/resources/test_files/quick_variable_test.py");
        System.out.println("[DEBUG] Absolute path: " + path.toAbsolutePath());
        System.out.println("[DEBUG] Exists: " + Files.exists(path));

        String sourceCode = Files.readString(path);

        // 2. Parse code with Tree-sitter
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(sourceCode);

        // 3. Set up registry and register variable extractor only
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("assignment", new VariableEntityExtractor());
        registry.register("typed_assignment", new VariableEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("module", new FileEntityExtractor());

        // 4. Walk
        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(
                registry,
                context,
                path.toString(),
                sourceCode);

        System.out.println("[DEBUG] Source:\n" + sourceCode);
        walker.walk(tree.getRootNode());

        // 5. Print extracted entities for manual inspection
        System.out.println("\n=== Extracted Entities ===");
        context.getAllEntities().forEach(e -> {
            // System.out.printf(
            // "%s | name=%s | type=%s | parent=%s | line=%d%n",
            // e.getType(),
            // e.getName(),
            // (e instanceof VariableEntity var)
            // ? var.getDeclaredType()
            // : null,
            // e.getParentId(),
            // e.getLocation().startLine());
            System.out.println(e);
        });

        // 6. Optionally add assertions (smoke test)
        List<CodeEntity> vars = context.getAllEntities().stream()
                .filter(e -> e.getType() == CodeEntityType.VARIABLE)
                .toList();

        assertFalse(vars.isEmpty(), "Expected at least one VariableEntity");

        boolean hasTyped = vars.stream().anyMatch(
                e -> ((com.jstarts.codegrapher.core.entities.VariableEntity) e).isTyped());
        boolean hasUntyped = vars.stream().anyMatch(
                e -> !((com.jstarts.codegrapher.core.entities.VariableEntity) e).isTyped());

        assertTrue(hasTyped && hasUntyped, "Should find both typed and untyped variables");
    }

}
