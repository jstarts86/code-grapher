package com.jstarts.codegrapher.extractors;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.FunctionEntity;
import com.jstarts.codegrapher.core.entities.PythonTypeEntity;
import com.jstarts.codegrapher.core.entities.VariableEntity;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class PythonTypeExtractionTest {
    static {
        LibraryLoader.load();
    }

    @Test
    void testPythonTypeExtraction() throws Exception {
        // 1. Load sample Python file
        Path path = Path.of("src/test/resources/test_files/python_type_extraction.py");
        assertTrue(Files.exists(path), "Test file should exist: " + path);

        String sourceCode = Files.readString(path);

        // 2. Parse code with Tree-sitter
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(sourceCode);

        // 3. Set up registry
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

        walker.walk(tree.getRootNode());

        List<CodeEntity> entities = context.getAllEntities();
        Map<String, PythonTypeEntity> typeRegistry = context.getTypeCanon().getRegistry();

        // 5. Assertions

        // Check that types were extracted
        assertFalse(typeRegistry.isEmpty(), "Should have extracted types");

        // Check specific types exist in canon
        assertTrue(typeRegistry.containsKey("int"), "Should contain 'int' type");
        assertTrue(typeRegistry.containsKey("float"), "Should contain 'float' type");
        assertTrue(typeRegistry.containsKey("List[str]"), "Should contain 'List[str]' type");
        assertTrue(typeRegistry.containsKey("Dict[str, int]"), "Should contain 'Dict[str, int]' type");

        // Check Variables
        VariableEntity x = (VariableEntity) findEntityByName(entities, "x");
        assertNotNull(x);
        assertEquals("int", x.getDeclaredType());
        assertNotNull(x.getTypeId());
        assertEquals(typeRegistry.get("int").getId(), x.getTypeId());

        VariableEntity names = (VariableEntity) findEntityByName(entities, "names");
        assertNotNull(names);
        assertEquals("List[str]", names.getDeclaredType()); // or whatever the text extraction gives
        assertEquals(typeRegistry.get("List[str]").getId(), names.getTypeId());

        // Check Functions
        FunctionEntity greet = (FunctionEntity) findEntityByName(entities, "greet");
        assertNotNull(greet);
        // Parameter p: Person
        FunctionEntity.Parameter p = greet.getParameters().get(0);
        assertEquals("p", p.getName());
        assertTrue(p.getTypeAnnotation().isPresent());
        assertEquals("Person", p.getTypeAnnotation().get());
        assertTrue(p.getTypeId().isPresent());
        assertEquals(typeRegistry.get("Person").getId(), p.getTypeId().get());

        // Return type None
        assertEquals("None", greet.getReturnType()); // Text extraction might be "None"
        assertNotNull(greet.getReturnTypeId());
        assertEquals(typeRegistry.get("None").getId(), greet.getReturnTypeId());

        // Check Complex Types
        // complex_structure: List[Dict[str, Union[int, float]]]
        // The signature might be normalized. Let's check if the type exists.
        // The parser builds signature as: List[Dict[str, Union[int, float]]]
        // Note: Union order might vary if not sorted, but list order is preserved.
        // My parser: Union signature is "A | B".
        // So: List[Dict[str, Union[int, float]]] -> List[Dict[str, int | float]]

        VariableEntity complex = (VariableEntity) findEntityByName(entities, "complex_structure");
        assertNotNull(complex);
        String complexTypeId = complex.getTypeId();
        assertNotNull(complexTypeId);

        // Find the type entity by ID
        PythonTypeEntity complexType = typeRegistry.values().stream()
                .filter(t -> t.getId().equals(complexTypeId))
                .findFirst().orElse(null);
        assertNotNull(complexType);
        System.out.println("Complex Type Signature: " + complexType.getSignature());
        assertTrue(complexType.getSignature().startsWith("List[Dict[str, "));

        // Check Modern Union (int | str)
        VariableEntity modernUnion = (VariableEntity) findEntityByName(entities, "modern_union");
        assertNotNull(modernUnion);
        PythonTypeEntity unionType = typeRegistry.values().stream()
                .filter(t -> t.getId().equals(modernUnion.getTypeId()))
                .findFirst().orElse(null);
        assertNotNull(unionType);
        assertEquals("Union", unionType.getName());
        // Signature should be "int | str"
        assertEquals("int | str", unionType.getSignature());

    }

    private CodeEntity findEntityByName(List<CodeEntity> entities, String name) {
        return entities.stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst()
                .orElse(null);
    }
}
