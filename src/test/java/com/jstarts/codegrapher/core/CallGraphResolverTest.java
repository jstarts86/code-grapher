package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;
import com.jstarts.codegrapher.extractors.*;
import org.junit.jupiter.api.Test;
import ch.usi.si.seart.treesitter.LibraryLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CallGraphResolverTest {

    static {
        LibraryLoader.load();
    }

    @Test
    void testResolveCallsInSampleProject() throws Exception {
        Path repoPath = Path.of("src/test/resources/test_repos/sample_project").toAbsolutePath();

        ExtractorRegistry registry = new ExtractorRegistry();
        registry.register("module", new FileEntityExtractor());
        registry.register("class_definition", new ClassEntityExtractor());
        registry.register("function_definition", new FunctionEntityExtractor());
        registry.register("import_statement", new ImportEntityExtractor());
        registry.register("import_from_statement", new ImportEntityExtractor());
        registry.register("call", new CallEntityExtractor());

        RepositoryProcessor processor = new RepositoryProcessor(registry);
        ExtractionContext context = processor.process(repoPath);
        List<CodeEntity> entities = context.getAllEntities();

        // Build Symbol Table
        GlobalSymbolTable symbolTable = new GlobalSymbolTable(entities, repoPath);

        // Resolve Imports first
        ImportResolver importResolver = new ImportResolver(symbolTable);
        importResolver.resolveImports(entities);

        // Resolve Calls
        CallGraphResolver callResolver = new CallGraphResolver(symbolTable);
        callResolver.resolveCalls(entities);

        // Verify Resolutions in main.py
        // main.py calls:
        // 1. log_info("Starting application...") -> imported from utils.logger
        // 2. User("alice", "alice@example.com") -> imported from models.user
        // 3. user.save() -> method call (harder, might not resolve yet)
        // 4. log_info("User created: " + user.username)

        FileEntity mainFile = (FileEntity) symbolTable.lookup("main");
        assertNotNull(mainFile);

        List<CallEntity> calls = entities.stream()
                .filter(e -> e instanceof CallEntity)
                .filter(e -> {
                    // Check if parent is mainFile or a function in mainFile
                    // For simplicity, let's just check if it's in the list and has mainFile as
                    // ancestor
                    // But parentId points to immediate parent.
                    // Let's filter by location file path?
                    return e.getLocation().filePath().endsWith("main.py");
                })
                .map(e -> (CallEntity) e)
                .collect(Collectors.toList());

        assertFalse(calls.isEmpty(), "main.py should have calls");

        boolean foundLogInfo = false;
        boolean foundUser = false;

        for (CallEntity call : calls) {
            if ("log_info".equals(call.getCallee())) {
                assertNotNull(call.getResolvedFunctionId(), "log_info call should be resolved");
                // Check if it points to log_info function
                CodeEntity target = entities.stream().filter(e -> e.getId().equals(call.getResolvedFunctionId()))
                        .findFirst().orElse(null);
                assertNotNull(target);
                assertEquals("log_info", target.getName());
                foundLogInfo = true;
            }

            if ("User".equals(call.getCallee())) {
                assertNotNull(call.getResolvedFunctionId(), "User constructor call should be resolved");
                CodeEntity target = entities.stream().filter(e -> e.getId().equals(call.getResolvedFunctionId()))
                        .findFirst().orElse(null);
                assertNotNull(target);
                assertEquals("User", target.getName());
                assertTrue(target instanceof ClassEntity);
                foundUser = true;
            }
        }

        assertTrue(foundLogInfo, "Should resolve log_info call");
        assertTrue(foundUser, "Should resolve User constructor call");
    }
}
