package com.jstarts.codegrapher;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.LibraryLoader;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import ch.usi.si.seart.treesitter.printer.TreePrinter;

import com.falkordb.ResultSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.falkordb.ingestors.RawSyntaxNodeIngestor;
import com.jstarts.codegrapher.graph.parser.GraphBuilder;
import com.jstarts.codegrapher.graph.parser.SingleFileTreeWalker;
import com.jstarts.codegrapher.graph.parser.extractors.AnnotationExtractor;
import com.jstarts.codegrapher.graph.parser.extractors.ClassExtractor;
import com.jstarts.codegrapher.raw.TsTreeBuilder;
import com.jstarts.codegrapher.raw.dto.RawSyntaxNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static {
        LibraryLoader.load();
    }

    }
}
