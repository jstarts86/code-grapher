package com.jstarts.codegrapher.graph.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.jstarts.codegrapher.graph.dto.ParsedFile;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.ImportDef;
import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.dto.node.TypeDef;

import ch.usi.si.seart.treesitter.Capture;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Query;
import ch.usi.si.seart.treesitter.QueryCursor;
import ch.usi.si.seart.treesitter.QueryMatch;
import ch.usi.si.seart.treesitter.Tree;

public class JavaParser {
    public String filePath;
    public Parser parser;

    public JavaParser(String filePath, Parser parser) {
        this.filePath = filePath;
        this.parser = parser;
    }

    public ParsedFile parse() throws IOException {
        String code = Files.readString(Path.of(filePath));
        Tree tree = parser.parse(code);
        Node root = tree.getRootNode();

        PackageDef packageDef = extractPackage(root, code);

        List<ImportDef> imports = new ArrayList<>();
        List<TypeDef> types = new ArrayList<>();

        return new ParsedFile(this.filePath, packageDef, imports, types);
    }

    private PackageDef extractPackage(Node root, String code) {
        String queryStr = "(package_declaration (scoped_identifier) @name)";
        AtomicReference<PackageDef> packageDefRef = new AtomicReference<>();

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);

            for (QueryMatch match : cursor) {
                Map<Capture, Collection<Node>> captures = match.getCaptures();
                for (Map.Entry<Capture, Collection<Node>> entry : captures.entrySet()) {
                    if (entry.getKey().getName().equals("name")) {
                        Collection<Node> nodes = entry.getValue();
                        if (!nodes.isEmpty()) {
                            Node node = nodes.iterator().next();
                            String packageName = code.substring(node.getStartByte(), node.getEndByte());
                            int startLine = node.getStartPoint().getRow() + 1;
                            int endLine = node.getEndPoint().getRow() + 1;
                            SourceLocation location = new SourceLocation(this.filePath, startLine, endLine);
                            packageDefRef.set(new PackageDef(packageName, location));
                            return packageDefRef.get();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return packageDefRef.get();

    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }
}
