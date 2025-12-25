package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;
import com.jstarts.codegrapher.extractors.*;
import org.junit.jupiter.api.Test;
import ch.usi.si.seart.treesitter.LibraryLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ImportResolverTest {

    static {
        LibraryLoader.load();
    }

    @Test
    void testResolveImportsInSampleProject() throws Exception {
        Path repoPath = Path.of("src/test/resources/test_repos/sample_project").toAbsolutePath();

        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("import_statement", new ImportEntityExtractor());
        registry.register("import_from_statement", new ImportEntityExtractor());

        RepositoryProcessor processor = new RepositoryProcessor(registry);
        ExtractionContext context = processor.process(repoPath);
        List<CodeEntity> entities = context.getAllEntities();

        // Build Symbol Table
        GlobalSymbolTable symbolTable = new GlobalSymbolTable(entities, repoPath);

        ImportResolver resolver = new ImportResolver(symbolTable);
        resolver.resolveImports(entities);

        // Verify Resolutions

        // 1. Check main.py imports
        // from utils import log_info
        // from models.user import User

        FileEntity mainFile = (FileEntity) symbolTable.lookup("main");
        assertNotNull(mainFile, "main.py should be found");

        List<ImportEntity> imports = entities.stream()
                .filter(e -> e instanceof ImportEntity)
                .filter(e -> e.getParentId().equals(mainFile.getId()))
                .map(e -> (ImportEntity) e)
                .collect(Collectors.toList());

        assertFalse(imports.isEmpty(), "main.py should have imports");

        boolean foundLogInfo = false;
        boolean foundUser = false;

        for (ImportEntity imp : imports) {
            Map<String, String> resolved = imp.getResolvedReferences();
            if (resolved == null)
                continue;

            if (resolved.containsKey("log_info")) {
                String targetId = resolved.get("log_info");
                CodeEntity target = entities.stream().filter(e -> e.getId().equals(targetId)).findFirst().orElse(null);
                assertNotNull(target);
                assertEquals("log_info", target.getName());
                assertTrue(target instanceof FunctionEntity);
                foundLogInfo = true;
            }

            if (resolved.containsKey("User")) {
                String targetId = resolved.get("User");
                CodeEntity target = entities.stream().filter(e -> e.getId().equals(targetId)).findFirst().orElse(null);
                assertNotNull(target);
                assertEquals("User", target.getName());
                assertTrue(target instanceof ClassEntity);
                foundUser = true;
            }
        }

        assertTrue(foundLogInfo, "Should resolve log_info import");
        assertTrue(foundUser, "Should resolve User import");

        // 2. Check utils/__init__.py relative imports
        // from .logger import log_info, log_error

        FileEntity utilsInit = (FileEntity) symbolTable.lookup("utils.__init__");
        // Wait, deriveQualifiedName removes __init__. So utils/__init__.py -> utils
        // But utils dir -> utils package.
        // Let's check how they are indexed.

        // PackageEntity for utils -> "utils"
        // FileEntity for utils/__init__.py -> "utils"
        // This collision might be an issue in SymbolTable if we use a simple Map.
        // Let's check GlobalSymbolTable logic.

        // If collision, last one wins.
        // We might want to distinguish.
    }
}
