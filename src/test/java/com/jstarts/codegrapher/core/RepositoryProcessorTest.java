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

public class RepositoryProcessorTest {

    static {
        LibraryLoader.load();
    }

    @Test
    void testProcessSampleProject() throws Exception {
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
        assertFalse(entities.isEmpty(), "Should extract entities");

        // Check Packages
        Map<String, PackageEntity> packages = entities.stream()
                .filter(e -> e instanceof PackageEntity)
                .map(e -> (PackageEntity) e)
                .collect(Collectors.toMap(CodeEntity::getName, e -> e));

        assertTrue(packages.containsKey("sample_project"), "Should find sample_project package");
        assertTrue(packages.containsKey("utils"), "Should find utils package");
        assertTrue(packages.containsKey("models"), "Should find models package");

        // Check Hierarchy
        PackageEntity utilsPkg = packages.get("utils");
        PackageEntity rootPkg = packages.get("sample_project");
        assertEquals(rootPkg.getId(), utilsPkg.getParentId(), "utils should be child of sample_project");

        // Check Files
        Map<String, FileEntity> files = entities.stream()
                .filter(e -> e instanceof FileEntity)
                .map(e -> (FileEntity) e)
                .collect(Collectors.toMap(CodeEntity::getName, e -> e));

        assertTrue(files.containsKey("logger.py"));
        FileEntity loggerFile = files.get("logger.py");
        assertEquals(utilsPkg.getId(), loggerFile.getParentId(), "logger.py should be in utils package");

        // Check content
        assertTrue(entities.stream().anyMatch(e -> e instanceof FunctionEntity && e.getName().equals("log_info")));
        assertTrue(entities.stream().anyMatch(e -> e instanceof ClassEntity && e.getName().equals("User")));
    }
}
