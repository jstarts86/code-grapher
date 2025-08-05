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
import com.jstarts.codegrapher.graph.dto.node.AnnotationDef;
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
    private List<AnnotationDef> extractAnnotation(Node root, String code) {
        String queryStr = """
        (marker_annotation name: (identifier) @name) @full
        (annotation name: (identifier) @name) @full
        (annotation name: (scoped_identifier) @name) @full
        """;
        List<AnnotationDef> annotationNodes = new ArrayList<>();

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);

            for (QueryMatch match : cursor) {
                Map<Capture, Collection<Node>> captures = match.getCaptures();
                Node fullNode = null;
                Node nameNode = null;

                for (Map.Entry<Capture, Collection<Node>> entry : captures.entrySet()) {
                    String captureName = entry.getKey().getName();
                    if ("full".equals(captureName)) {
                        fullNode = entry.getValue().iterator().next();
                    } else if ("name".equals(captureName)) {
                        nameNode = entry.getValue().iterator().next();
                    }
                }

                if (fullNode != null && nameNode != null) {
                    String typeName = code.substring(nameNode.getStartByte(), nameNode.getEndByte());
                    String annotationname = code.substring(fullNode.getStartByte(), fullNode.getEndByte());

                    List<String> arguments = parseArguments(fullNode, code);

                    AnnotationDef annotationNode = new AnnotationDef(typeName, annotationname, arguments);
                    annotationNodes.add(annotationNode);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return annotationNodes;
    }
    private List<String> parseArguments(Node fullNode, String code) {
        List<String> argumentNames = new ArrayList<>();
        String argumentNameQuery = "(element_value_pair key: (identifier) @key)";

        try (Query query = Query.getFor(Language.JAVA, argumentNameQuery)) {
            QueryCursor cursor = fullNode.walk(query);
            for (QueryMatch match : cursor) {
                Map<Capture, Collection<Node>> captures = match.getCaptures();
                for (Map.Entry<Capture, Collection<Node>> entry : captures.entrySet()) {
                    String captureName = entry.getKey().getName();
                    if ("key".equals(captureName)) {
                        Node keyNode = entry.getValue().iterator().next();
                        String key = code.substring(keyNode.getStartByte(), keyNode.getEndByte());
                        argumentNames.add(key);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return argumentNames;
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
