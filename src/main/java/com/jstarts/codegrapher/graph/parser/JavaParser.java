package com.jstarts.codegrapher.graph.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.transform.Source;

import com.jstarts.codegrapher.graph.dto.ParsedFile;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.ImportDef;
import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.ClassDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.TypeDef;

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

        return new ParsedFile(this.filePath, packageDef, imports, types, null);
    }

    public PackageDef extractPackage(Node root, String code) {
        String queryStr = "(package_declaration (scoped_identifier) @name)";
        // AtomicReference<PackageDef> packageDefRef = new AtomicReference<>();

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
                            // packageDefRef.set(new PackageDef(packageName, location));
                            // return packageDefRef.get();
                            return new PackageDef(packageName, location);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;

    }
    public List<ClassDef> extractClass(Node root, String code) {
        String queryStr = "(class_declaration (modifiers)* @classModifier name: (identifier) @className)";
        List<ClassDef> classDefs = new ArrayList<>();

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);
            Node nameNode = null;
            String modifierString = "";
            String className = "";
            
            // Each iteration processes one class declaration
            for(QueryMatch match : cursor) {
                // Extracted data for current class_declaration
                Map<Capture, Collection<Node>> captures = match.getCaptures();
                // Process all captures in match
                for (Map.Entry<Capture, Collection<Node>> entry : captures.entrySet()) {
                    String captureName = entry.getKey().getName();
                    for(Node node : entry.getValue()) {
                        String modifierOrClass = code.substring(node.getStartByte(), node.getEndByte());

                        if(captureName.equals("classModifier")) {
                            modifierString = modifierOrClass; 
                        } else if (captureName.equals("className")) {
                            className = modifierOrClass;
                            nameNode = node;
                        }
                    }
                }
                if (className != null && nameNode != null) {
                    int startLine = nameNode.getStartPoint().getRow() + 1;
                    int endLine = nameNode.getEndPoint().getRow() + 1;

                    SourceLocation location = new SourceLocation(this.filePath, startLine, endLine);
                    System.err.println("DEBUGPRINT[51]: JavaParser.java:118: location=" + location);

                    ClassDef classDef = new ClassDef(className,modifierString,location,Boolean.FALSE);
                    classDefs.add(classDef);
                }
            }
        }
        return classDefs;
        
 // (class_declaration ; [26, 0] - [102, 1]
 //    (modifiers) ; [26, 0] - [26, 6]
 //    name: (identifier) ; [26, 13] - [26, 23]


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
