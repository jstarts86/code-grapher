package com.jstarts.codegrapher.extractors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;

class ClassEntityExtractionTest {

    private ClassEntityExtractor extractor;
    private String pythonSource;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        extractor = new ClassEntityExtractor();
        Path path = Paths.get(getClass().getClassLoader().getResource("test_files/test.py").toURI());
        pythonSource = Files.readString(path);
    }

    @Test
    void testExtractClassEntities() {
        // Tree tree = parser.parse(pythonSource);
        // Node node = tree.getRootNode();

        // List<CodeEntityExtractor> applicableExtractors =
        // registry.get(node.getType());

        // assertEquals(2, classEntities.size());
        //
        // ClassEntity shapeClass = classEntities.stream()
        // .filter(c -> "Shape".equals(c.getName()))
        // .findFirst()
        // .orElse(null);
        // assertNotNull(shapeClass);
        // assertEquals("Shape", shapeClass.getName());
        //
        // ClassEntity circleClass = classEntities.stream()
        // .filter(c -> "Circle".equals(c.getName()))
        // .findFirst()
        // .orElse(null);
        // assertNotNull(circleClass);
        // assertEquals("Circle", circleClass.getName());
    }
}
