package com.jstarts.codegrapher.extractors;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.parsers.PythonTreeWalker;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;

import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DecoratorEntityExtractorTest {

    static {
        LibraryLoader.load();
    }

    @Test
    void extractDecorators() throws Exception {
        Path path = Path.of("src/test/resources/test_files/decorator.py");
        String source = Files.readString(path);
        System.out.println(source);

        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(source);

        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("decorated_definition", new DecoratorEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("module", new FileEntityExtractor());

        ExtractionContext context = new ExtractionContext();
        PythonTreeWalker walker = new PythonTreeWalker(registry, context, path.toString(), source);

        walker.walk(tree.getRootNode());

        List<CodeEntity> decorators = context.getAllEntities().stream()
                .filter(e -> e.getType() == CodeEntityType.DECORATOR)
                .toList();

        assertFalse(decorators.isEmpty(), "Expected at least one decorator entity");

        decorators.forEach(d -> {
            // System.out.printf(
            // "Decorator: %s | parent=%s | line=%d%n",
            // ((com.jstarts.codegrapher.core.entities.DecoratorEntity) d).getExpression(),
            // d.getParentId(),
            // d.getLocation().startLine());
            System.out.println(d);
        });
    }
}
