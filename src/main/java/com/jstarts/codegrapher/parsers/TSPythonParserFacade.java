package com.jstarts.codegrapher.parsers;

import java.io.File;
import java.nio.file.Path;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import lombok.Getter;

@Getter
public class TSPythonParserFacade implements AutoCloseable {
    private static final Language LANGUAGE = Language.PYTHON;
    private final Parser parser;

    public TSPythonParserFacade() {
        this.parser = Parser.getFor(LANGUAGE);
    }

    public Tree parse(String code) {
        return this.getParser().parse(code);
    }
    public Tree parse(Path path) {
        return this.getParser().parse(path);
    }

    @Override
    public void close() {
        if (parser != null){
            parser.close();;
        }
    }
}
