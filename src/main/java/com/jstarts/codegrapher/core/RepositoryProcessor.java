package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;
import com.jstarts.codegrapher.extractors.ExtractionContext;
import com.jstarts.codegrapher.extractors.ExtractorRegistry;
import com.jstarts.codegrapher.parsers.PythonTreeWalker;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public class RepositoryProcessor {

    private static final Set<String> IGNORED_DIRS = Set.of(".git", ".idea", "__pycache__", "venv", "node_modules",
            "build", "dist", ".gradle");

    private final ExtractorRegistry registry;
    private final Parser parser;

    public RepositoryProcessor(ExtractorRegistry registry) {
        this.registry = registry;
        this.parser = Parser.getFor(Language.PYTHON);
    }

    public ExtractionContext process(Path rootPath) throws IOException {
        ExtractionContext context = new ExtractionContext();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getFileName() != null && IGNORED_DIRS.contains(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                PackageEntity packageEntity = createPackageEntity(dir, context);
                context.pushContext(packageEntity);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".py")) {
                    processFile(file, context);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (dir.getFileName() != null && IGNORED_DIRS.contains(dir.getFileName().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                context.popContext();
                return FileVisitResult.CONTINUE;
            }
        });

        return context;
    }

    private PackageEntity createPackageEntity(Path dir, ExtractionContext context) {
        SourceLocation loc = SourceLocation.builder()
                .filePath(dir.toAbsolutePath().toString())
                .startLine(0).endLine(0)
                .startCol(0).endCol(0)
                .startByte(0).endByte(0)
                .build();

        String parentId = context.peekContext() != null ? context.peekContext().getId() : null;

        return new PackageEntity.Builder()
                .name(dir.getFileName() != null ? dir.getFileName().toString() : dir.toString())
                .id(CodeEntity.generateId(loc))
                .location(loc)
                .parentId(parentId)
                .build();
    }

    private void processFile(Path file, ExtractionContext context) throws IOException {
        String sourceCode = Files.readString(file);
        Tree tree = parser.parse(sourceCode);

        PythonTreeWalker walker = new PythonTreeWalker(registry, context, file.toAbsolutePath().toString(), sourceCode);
        walker.walk(tree.getRootNode());
    }
}
