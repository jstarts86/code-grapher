package com.jstarts.codegrapher.graph.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RawJavaRepoParser {

    private final Path rootPath;

    public RawJavaRepoParser(String rootPath) {
        this.rootPath = Paths.get(rootPath);
    }

    public List<Path> findJavaFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(this.rootPath)) {
            return stream
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }
}
