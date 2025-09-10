package com.jstarts.codegrapher.falkordb.ingestors;

import com.falkordb.Record;
import com.falkordb.ResultSet;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.raw.dto.TsNode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TsRawNodeIngestor {
    private final FalkorConfig falkorConfig;

    public TsRawNodeIngestor(FalkorConfig falkorConfig) {
        this.falkorConfig = falkorConfig;
    }

    public void ingest(TsNode rootNode) {
        ingestNodeRecursive(rootNode, null);
    }

    public Long createGraphNode(TsNode tsNode) {
        Map<String, Object> params = Map.of(
            "type", tsNode.getType(),
            "text", tsNode.getText(),
            "startByte", tsNode.getStartByte(),
            "endByte", tsNode.getEndByte(),
            "startLine", tsNode.getStartLine(),
            "endLine", tsNode.getEndLine(),
            "startCol", tsNode.getStartCol(),
            "endCol", tsNode.getEndCol()

        );

        ResultSet result =  falkorConfig.executeQuery("CREATE (n:tsNode {type: $type, text: $text, startByte: $startByte, endByte: $endByte, startLine: $startLine, endLine: $endLine, startCol: $startCol, endCol: $endCol})" 
            + "RETURN id(n) as nodeId"
            ,params);
        for (Record record : result) {
            Long nodeId = record.getValue("nodeId");
            if (nodeId != null) {
                return nodeId;
            }

        }
        throw new RuntimeException("No node ID returned when creating tsNode");

    }

    public void createGraphRelationship(Long parentId, Long childId, String relType) {

    }

    private void ingestNodeRecursive(TsNode tsNode, String parentId) {
        String nodeId = UUID.randomUUID().toString();
        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        params.put("type", tsNode.getType());
        params.put("text", tsNode.getText());
        params.put("startByte", tsNode.getStartByte());
        params.put("endByte", tsNode.getEndByte());
        params.put("startLine", tsNode.getStartLine());
        params.put("endLine", tsNode.getEndLine());
        params.put("startCol", tsNode.getStartCol());
        params.put("endCol", tsNode.getEndCol());

        String createQuery = "CREATE (:tsNode {nodeId: $nodeId, type: $type, text: $text, startByte: $startByte, endByte: $endByte, startLine: $startLine, endLine: $endLine, startCol: $startCol, endCol: $endCol})";
        falkorConfig.executeQuery(createQuery, params);

        if (parentId != null) {
            String edgeQuery = "MATCH (parent:tsNode {nodeId: $parentId}), (child:tsNode {nodeId: $childId}) CREATE (parent)-[:CONTAINS]->(child)";
            Map<String, Object> edgeParams = Map.of("parentId", parentId, "childId", nodeId);
            falkorConfig.executeQuery(edgeQuery, edgeParams);
        }

        for (TsNode child : tsNode.getChildren()) {
            ingestNodeRecursive(child, nodeId);
        }
    }

    private void ingestNodeIteratively(TsNode root) {

    }
}
