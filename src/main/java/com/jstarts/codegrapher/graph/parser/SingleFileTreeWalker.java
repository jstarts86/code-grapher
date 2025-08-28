package com.jstarts.codegrapher.graph.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jstarts.codegrapher.graph.dto.node.NodeDef;
import com.jstarts.codegrapher.graph.parser.extractors.CodeEntityExtractor;

import ch.usi.si.seart.treesitter.Capture;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Query;
import ch.usi.si.seart.treesitter.QueryCursor;
import ch.usi.si.seart.treesitter.QueryMatch;
import ch.usi.si.seart.treesitter.Tree;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SingleFileTreeWalker {

    private Map<String, List<CodeEntityExtractor>> registry = new HashMap<>();
    private String sourceCode;
    private String filePath;
    private String packageName;
    private Node root;

    public SingleFileTreeWalker(String sourceCode, String filePath) {
        this.sourceCode = sourceCode;
        this.filePath = filePath;
    };
    public void setRootNode(String code) {
        
        Parser parser = Parser.getFor(Language.JAVA);
        Tree tree = parser.parse(code);
        this.setRoot(tree.getRootNode());
    }

    public void register(String nodeType, CodeEntityExtractor extractor) {
        this.registry.computeIfAbsent(nodeType, k -> new ArrayList<>()).add(extractor);
    }

    public String findPackageName( String code) {
        String queryStr = "(package_declaration (scoped_identifier) @name)";

        try (Query query = Query.getFor(Language.JAVA, queryStr);
            QueryCursor cursor = this.getRoot().walk(query)) {

            for (QueryMatch match : cursor) {
                Node nameNode = match.getCaptures().entrySet().stream()
                    .filter(e -> e.getKey().getName().equals("name"))
                    .flatMap(e -> e.getValue().stream())
                    .findFirst()
                    .orElse(null);

                if (nameNode != null) {
                    return code.substring(
                        nameNode.getStartByte(),
                        nameNode.getEndByte()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Could not find packageName: " + e.getMessage());
        }

        return null; // default package
    }

    public void walk(Node node) {
        List<CodeEntityExtractor> applicableExtractors = registry.get(node.getType());
        if (applicableExtractors != null) {
            for (CodeEntityExtractor extractor : applicableExtractors) {
                extractor.extract(node, this.sourceCode, this.filePath, this.packageName);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i));
        }
    }
    
}
