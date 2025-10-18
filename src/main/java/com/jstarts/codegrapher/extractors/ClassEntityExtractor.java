package com.jstarts.codegrapher.extractors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.jstarts.codegrapher.core.entities.ClassEntity;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;

public class ClassEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        return "class_definition".equals(nodeType);
    }

    @Override
    public Optional<CodeEntity> extract(Node node, ExtractionContext context, String filePath, String sourceCode) {
        return Optional.ofNullable(node.getChildByFieldName("name"))
                .flatMap(nameNode -> buildClassEntity(nameNode, node, context, filePath, sourceCode))
                .map(e -> (CodeEntity) e);
    }

    private Optional<ClassEntity> buildClassEntity(Node nameNode, Node classNode, ExtractionContext context,
            String filePath, String sourceCode) {
        try {
            String className = sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
            SourceLocation location = buildLocation(filePath, nameNode);
            List<String> superClasses = extractSuperClasses(classNode, sourceCode);
            List<String> typeParameters = extractTypeParameters(classNode, sourceCode);
            ClassEntity classEntity = new ClassEntity.Builder()
                    .name(className)
                    .id(CodeEntity.generateId(location))
                    .location(location)
                    .parentId(context.peekContext().getId())
                    .superClasses(superClasses)
                    .type(CodeEntityType.CLASS)
                    .types(typeParameters)
                    .build();
            return Optional.of(classEntity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private SourceLocation buildLocation(String filePath, Node nameNode) {
        return SourceLocation.builder()
                .filePath(filePath)
                .startLine(nameNode.getStartPoint().getRow() + 1)
                .endLine(nameNode.getEndPoint().getRow() + 1)
                .startByte(nameNode.getStartByte())
                .endByte(nameNode.getEndByte())
                .startCol(nameNode.getStartPoint().getColumn())
                .endCol(nameNode.getEndPoint().getColumn())
                .build();
    }

    // (class_definition ; [52, 0] - [64, 45]
    // name: (identifier) ; [52, 6] - [52, 12]
    // superclasses: (argument_list ; [52, 12] - [52, 28]
    // (identifier) ; [52, 13] - [52, 18]
    // (identifier)) ; [52, 20] - [52, 27]
    private List<String> extractSuperClasses(Node classNode, String sourceCode) {
        return Optional.ofNullable(classNode.getChildByFieldName("superclasses"))
                .map(Node::getChildren)
                .map(children -> children.stream()
                        .map(child -> sourceCode.substring(child.getStartByte(), child.getEndByte()))
                        .toList())
                .orElse(List.of());
        // return superClassNode.getChildren().stream()
        // .map(child -> sourceCode.substring(child.getStartByte(), child.getEndByte()))
        // .toList())

    }

    private List<String> extractTypeParameters(Node classNode, String sourceCode) {
        return Optional.ofNullable(classNode.getChildByFieldName("type_parameters"))
                .map(Node::getChildren)
                .map(children -> children.stream()
                        .map(child -> sourceCode.substring(child.getStartByte(), child.getEndByte()))
                        .toList())
                .orElse(List.of());
    }

    public static void main(String[] args) throws IOException {

        Path path = Paths.get(new String(
                Objects.requireNonNull(
                        ClassLoader.getSystemResourceAsStream("test/test.py").readAllBytes())));
        String pythonSource = Files.readString(path);
        Parser parser = Parser.getFor(Language.PYTHON);
        Tree tree = parser.parse(pythonSource);

    }

}
