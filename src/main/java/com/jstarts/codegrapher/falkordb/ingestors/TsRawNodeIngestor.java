package com.jstarts.codegrapher.falkordb.ingestors;

import java.util.Map;

import com.falkordb.graph_entities.Node;
import com.jstarts.codegrapher.falkordb.FalkorConfig;
import com.jstarts.codegrapher.raw.dto.TsNode;

public class TsRawNodeIngestor {
    private FalkorConfig falkorConfig;

    public void TsNodeToGraph(TsNode tsNode) {
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
        falkorConfig.executeQuery("");

        falkorConfig.executeQuery("CREATE (:tsNode {type: $type, text: $text, startByte: $startByte, endByte: $endByte, startLine: $startLine, endLine: $endLine, startCol: $startCol, endCol: $endCol})"
            ,params);
    }

    // public void createEdges(TsNode rootNode) {
    //
    //
    // }


}
