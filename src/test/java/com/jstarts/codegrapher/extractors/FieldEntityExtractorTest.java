package com.jstarts.codegrapher.extractors;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.FieldEntity;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class FieldEntityExtractorTest {
    static {
        LibraryLoader.load();
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of("src/test/resources/test_files/field.py");
        String source = Files.readString(path);
        Parser parser = Parser.getFor(Language.PYTHON);

        Tree tree = parser.parse(source);
        System.out.println(source);
        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("assignment", new FieldEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("module", new FileEntityExtractor());
        registry.register("assignment", new VariableEntityExtractor());

        ExtractionContext ctx = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(registry, ctx, path.toString(), source);
        walker.walk(tree.getRootNode());

        System.out.println("\n=== Extracted Entities ===");
        ctx.getAllEntities().forEach(e -> {
            System.out.printf(
                    "%s | name=%s | type=%s | parent=%s | line=%d%n",
                    e.getType(),
                    e.getName(),
                    (e instanceof FieldEntity var)
                            ? var.getDeclaredType()
                            : null,
                    e.getParentId(),
                    e.getLocation().startLine());
            // System.out.println(e);
        });
        List<CodeEntity> fields = ctx.getAllEntities().stream()
                .filter(e -> e.getType() == CodeEntityType.FIELD)
                .toList();

        assertFalse(fields.isEmpty(), "Expected class fields");

        fields.forEach(f -> System.out.printf(
                "%s | name=%s | parent=%s | typed=%s%n",
                f.getType(),
                f.getName(),
                f.getParentId(),
                ((FieldEntity) f).isTyped()));

        // Ensure method variable z was not classified as FIELD
        assertNull(fields.stream().filter(f -> f.getName().equals("z")).findAny().orElse(null),
                "Method-local variables should not be classified as fields");

    }

    @Test
    void testFieldExtraction() throws Exception {
        FieldEntityExtractorTest.main(new String[0]);

    }

}
