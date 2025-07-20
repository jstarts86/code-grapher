
package com.jstarts.codegrapher.graph.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jstarts.codegrapher.graph.dto.ParsedFile;

import ch.usi.si.seart.treesitter.Parser;

public class JavaParser {
    public String filePath;
    public Parser parser;

    public JavaParser(String filePath, Parser parser) {
        this.filePath = filePath;
        this.parser = parser;
    }

    public ParsedFile parse() throws IOException {
        String code = Files.readString(Path.of(filePath));
        return null;
    }

}
