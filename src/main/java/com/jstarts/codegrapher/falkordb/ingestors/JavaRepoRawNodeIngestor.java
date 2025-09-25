package com.jstarts.codegrapher.falkordb.ingestors;

import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Tree;
import com.falkordb.Record;
import com.falkordb.ResultSet;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.graph.parser.RawJavaRepoParser;
import com.jstarts.codegrapher.raw.TsTreeBuilder;
import com.jstarts.codegrapher.raw.dto.RawSyntaxNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class JavaRepoRawNodeIngestor {

    private final FalkorConfig falkorConfig;
    private final String projectRoot;

    private static class StackFrame {
        RawSyntaxNode treeNode;
        Long dbNodeId;
        public StackFrame(RawSyntaxNode treeNode, Long dbNodeId) {
            this.treeNode = treeNode;
            this.dbNodeId = dbNodeId;
        }
    }

    public JavaRepoRawNodeIngestor(FalkorConfig falkorConfig, String projectRoot) {
        this.falkorConfig = falkorConfig;
        this.projectRoot = projectRoot;
    }

    public void ingestRepository() throws IOException {
        RawJavaRepoParser repoParser = new RawJavaRepoParser(projectRoot);
        List<Path> javaFiles = repoParser.findJavaFiles();
        
        try (Parser treeSitterParser = Parser.getFor(Language.JAVA)) {
            for (Path filePath : javaFiles) {
                ingestFile(treeSitterParser, filePath);
            }
        }
    }

    private void ingestFile(Parser treeSitterParser, Path filePath) throws IOException {
        System.out.println("Ingesting file: " + filePath);
        String code = Files.readString(filePath);

        Long fileDbId = createFileNode(filePath);

        TsTreeBuilder builder = new TsTreeBuilder();
        Tree tree = treeSitterParser.parse(code);
        RawSyntaxNode rootNode = builder.build(tree, code);

        if (rootNode != null) {
            ingestAst(rootNode, fileDbId);
        }
    }
    
    private Long createFileNode(Path filePath) {
        String relativePath = Path.of(projectRoot).relativize(filePath).toString();
        Map<String, Object> params = Map.of("path", relativePath);
        ResultSet result = falkorConfig.executeQuery("CREATE (f:File {path: $path}) RETURN id(f) as fileId", params);
        
        if (result.iterator().hasNext()) {
            Record record = result.iterator().next();
            if (record != null && record.containsKey("fileId")) {
                return (Long) record.getValue("fileId");
            }
        }
        throw new RuntimeException("Could not create File node for: " + filePath);
    }

    private void ingestAst(RawSyntaxNode root, Long fileDbId) {
        Deque<StackFrame> stack = new ArrayDeque<>();
        
        Long rootDbId = createGraphNode(root);
        stack.push(new StackFrame(root, rootDbId));
        
        createGraphRelationship(fileDbId, rootDbId, "HAS_AST_ROOT");

        while(!stack.isEmpty()) {
            StackFrame frame = stack.pop();
            RawSyntaxNode current = frame.treeNode;
            Long currentId = frame.dbNodeId;

            for(RawSyntaxNode child : current.getChildren()) {
                Long childId = createGraphNode(child);
                createGraphRelationship(currentId, childId, "CONTAINS");
                stack.push(new StackFrame(child, childId));
            }
        }
    }

    private Long createGraphNode(RawSyntaxNode tsNode) {
        Map<String, Object> params = Map.of(
            "type", tsNode.getType(),
            "startByte", tsNode.getStartByte(),
            "endByte", tsNode.getEndByte(),
            "startLine", tsNode.getStartLine(),
            "endLine", tsNode.getEndLine(),
            "startCol", tsNode.getStartCol(),
            "endCol", tsNode.getEndCol()
        );

        ResultSet result = falkorConfig.executeQuery(
            "CREATE (n:tsNode {type: $type, startByte: $startByte, endByte: $endByte, startLine: $startLine, endLine: $endLine, startCol: $startCol, endCol: $endCol}) RETURN id(n) as nodeId",
            params
        );
        
        if (result.iterator().hasNext()) {
            Record record = result.iterator().next();
            if (record != null && record.containsKey("nodeId")) {
                return (Long) record.getValue("nodeId");
            }
        }
        throw new RuntimeException("No node ID returned when creating tsNode");
    }

    private void createGraphRelationship(Long parentId, Long childId, String relType) {
        Map<String, Object> params = Map.of(
            "parentId", parentId,
            "childId", childId
        );

        falkorConfig.executeQuery(
            "MATCH (p), (c) WHERE id(p) = $parentId AND id(c) = $childId CREATE (p)-[:" + relType + "]->(c)",
            params
        );
    }
}
