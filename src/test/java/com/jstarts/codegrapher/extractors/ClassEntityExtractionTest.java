package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.ClassEntity;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.parsers.ASTCursorFacade;
import com.jstarts.codegrapher.parsers.TSPythonParserFacade;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ClassEntityExtractionTest {

    private TSPythonParserFacade parser;
    private ClassEntityExtractor extractor;
    private String pythonSource;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        extractor = new ClassEntityExtractor();
        Path path = Paths.get(getClass().getClassLoader().getResource("test/test.py").toURI());
        pythonSource = Files.readString(path);
    }

    @Test
    void testExtractClassEntities() {
        Tree tree = parser.parse(pythonSource);
        Node node = tree.getRootNode();

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
