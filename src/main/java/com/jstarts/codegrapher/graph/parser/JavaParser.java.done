package com.jstarts.codegrapher.graph.parser;

import java.io.IOException;
import java.lang.reflect.AccessFlag.Location;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jstarts.codegrapher.graph.dto.ParsedFile;
import com.jstarts.codegrapher.graph.dto.metadata.SourceLocation;
import com.jstarts.codegrapher.graph.dto.node.AnnotationDef;
import com.jstarts.codegrapher.graph.dto.node.FieldDef;
import com.jstarts.codegrapher.graph.dto.node.ImportDef;
import com.jstarts.codegrapher.graph.dto.node.MethodDef;
import com.jstarts.codegrapher.graph.dto.node.PackageDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.ClassDef;
import com.jstarts.codegrapher.graph.dto.node.typedef.TypeDef;
import com.jstarts.codegrapher.graph.dto.usage.TypeUsage;

import ch.usi.si.seart.treesitter.Capture;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Query;
import ch.usi.si.seart.treesitter.QueryCursor;
import ch.usi.si.seart.treesitter.QueryMatch;
import ch.usi.si.seart.treesitter.Tree;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JavaParser {
    public String filePath;
    public Parser parser;

    public JavaParser(String filePath, Parser parser) {
        this.filePath = filePath;
        this.parser = parser;
    }

    // public ParsedFile parse() throws IOException {
    //     String code = Files.readString(Path.of(filePath));
    //     Tree tree = parser.parse(code);
    //     Node root = tree.getRootNode();
    //
    //     PackageDef packageDef = extractPackage(root, code);
    //
    //     List<ImportDef> imports = new ArrayList<>();
    //     List<TypeDef> types = new ArrayList<>();
    //     return new ParsedFile(this.filePath, packageDef, imports, types, typeUsages, null);
    // }

    public PackageDef extractPackage(Node root, String code) {
        String queryStr = "(package_declaration (scoped_identifier) @name)";

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);

            for (QueryMatch match : cursor) {
                for (Map.Entry<Capture, Collection<Node>> entry : match.getCaptures().entrySet()) {
                    if (entry.getKey().getName().equals("name")) {
                        Collection<Node> nodes = entry.getValue();
                        if (!nodes.isEmpty()) {
                            Node node = nodes.iterator().next();
                            String packageName = code.substring(node.getStartByte(), node.getEndByte());
                            
                            int startLine = node.getStartPoint().getRow() + 1;
                            int endLine = node.getEndPoint().getRow() + 1;
                            SourceLocation location = new SourceLocation(this.filePath, startLine, endLine);
                            return new PackageDef(packageName, location);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            
            for(QueryMatch match : cursor) {
                Map<Capture, Collection<Node>> captures = match.getCaptures();
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

                    ClassDef classDef = new ClassDef(className,modifierString,location,"");
                    classDefs.add(classDef);
                }
            }
        }
        return classDefs;
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
                for (Map.Entry<Capture, Collection<Node>> entry : match.getCaptures().entrySet()) {
                    if ("key".equals(entry.getKey().getName())) {
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

    private List<FieldDef> extractFieldsByQuery(Node root, String code, String classFQN) {
        String queryStr = """
                (field_declaration 
                    (modifiers)? @mods
                    type: (_) @type
                    (variable_declarator name: (identifier) @name)
                )
                """;

        List<FieldDef> fieldDefs = new ArrayList<>();

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);
            for (QueryMatch match : cursor) {
                Node nameNode = null;
                Node typeNode = null;
                Node modsNode = null;

                for (Map.Entry<Capture, Collection<Node>> entry : match.getCaptures().entrySet()) {
                    String capName = entry.getKey().getName();
                    Node node = entry.getValue().iterator().next();
                    switch (capName) {
                        case "name" -> nameNode = node;
                        case "type" -> typeNode = node;
                        case "mods" -> modsNode = node;
                    }
                }

                String fieldName = nameNode != null ? code.substring(nameNode.getStartByte(), nameNode.getEndByte()) : "";
                String type = typeNode != null ? code.substring(typeNode.getStartByte(), typeNode.getEndByte()) : "Object";
                String modsText = modsNode != null ? code.substring(modsNode.getStartByte(), modsNode.getEndByte()) : "";

                boolean isStatic = modsText.contains("static");
                String accessModifier = "default";
                if (modsText.contains("public")) accessModifier = "public";
                else if (modsText.contains("private")) accessModifier = "private";
                else if (modsText.contains("protected")) accessModifier = "protected";

                fieldDefs.add(new FieldDef(fieldName, classFQN, type, isStatic, accessModifier));
            }
        }
        return fieldDefs;
    }

    private List<MethodDef> extractMethodsByQuery(Node root, String code, String classFQN) {
        String queryStr = """
                (method_declaration 
                    (modifiers)? @mods
                    type: (_) @rtype
                    name: (identifier) @name
                    parameters: (formal_parameters) @params
                )
                """;

        List<MethodDef> methodDefs = new ArrayList<>();

        try (Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);
            for (QueryMatch match : cursor) {
                Node modsNode = null;
                Node rtypeNode = null;
                Node nameNode = null;
                Node paramsNode = null;

                for (Map.Entry<Capture, Collection<Node>> entry : match.getCaptures().entrySet()) {
                    String capName = entry.getKey().getName();
                    Node node = entry.getValue().iterator().next();
                    switch (capName) {
                        case "mods" -> modsNode = node;
                        case "rtype" -> rtypeNode = node;
                        case "name" -> nameNode = node;
                        case "params" -> paramsNode = node;
                    }
                }

                String methodName = nameNode != null ? code.substring(nameNode.getStartByte(), nameNode.getEndByte()) : "";
                String fqName = classFQN + "." + methodName;
                String returnType = rtypeNode != null ? code.substring(rtypeNode.getStartByte(), rtypeNode.getEndByte()) : "void";

                List<String> parameterList = new ArrayList<>();
                if (paramsNode != null) {
                    for (int i = 0; i < paramsNode.getNamedChildCount(); i++) {
                        Node param = paramsNode.getNamedChild(i);
                        parameterList.add(code.substring(param.getStartByte(), param.getEndByte()));
                    }
                }

                String modsText = modsNode != null ? code.substring(modsNode.getStartByte(), modsNode.getEndByte()) : "";
                boolean isStatic = modsText.contains("static");
                String accessModifier = "default";
                if (modsText.contains("public")) accessModifier = "public";
                else if (modsText.contains("private")) accessModifier = "private";
                else if (modsText.contains("protected")) accessModifier = "protected";

                methodDefs.add(new MethodDef(methodName, fqName, parameterList, returnType, isStatic, accessModifier));
            }
        }
        return methodDefs;
    }

    private String extractClassFullName(Node root, String code) {
        String packageName = "";
        String className = "";

        for (int i = 0; i < root.getChildCount(); i++) {
            Node child = root.getChild(i);
            if ("package_declaration".equals(child.getType())) {
                Node nameNode = child.getChild(1);
                if (nameNode != null) {
                    packageName = code.substring(nameNode.getStartByte(), nameNode.getEndByte());
                }
            }
            if ("class_declaration".equals(child.getType())) {
                Node nameNode = child.getChildByFieldName("name");
                if (nameNode != null) { 
                    className = code.substring(nameNode.getStartByte(), nameNode.getEndByte());
                }
            }
        }

        return !packageName.isEmpty() ? packageName + "." + className : className;
    }

        // (type_identifier) @type
        // (scoped_type_identifier name: (type_identifier) @type)
        // (primitive_type) @type
        // (type_arguments (type_identifier) @type)
        // (array_type element: (type_identifier) @type)

    private List<TypeUsage> extractDeclaredType(Node root, String code) {

        List<TypeUsage> extractedTypeUsage = new ArrayList<>();

        String queryStr = """
        (type_identifier) @type
        """;

        try(Query query = Query.getFor(Language.JAVA, queryStr)) {
            QueryCursor cursor = root.walk(query);
            for (QueryMatch match : cursor) {
                Map<Capture, Collection<Node>> captures = match.getCaptures();
                for (Map.Entry<Capture, Collection<Node>> entry : captures.entrySet()) {
                    Collection<Node> nodes = entry.getValue();
                    String captureName = entry.getKey().getName();
                    for (Node node : nodes) {
                        String typeName = code.substring(node.getStartByte(),node.getEndByte());
                        int startLine = node.getStartPoint().getRow() + 1;
                        int endLine = node.getEndPoint().getRow() + 1;
                        SourceLocation location = new SourceLocation(filePath, startLine, endLine);

                        TypeUsage usage = new TypeUsage(typeName, location, "" , typeUseKind)

                    }
                    String captureName = entry.getKey().getName();
                }

            }

            }



        return extractedTypeUsage;

    }
}
