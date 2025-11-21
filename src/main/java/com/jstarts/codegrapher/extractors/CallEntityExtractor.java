package com.jstarts.codegrapher.extractors;

import java.util.ArrayList;
import java.util.List;

import com.jstarts.codegrapher.core.entities.CallEntity;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import ch.usi.si.seart.treesitter.Node;

public class CallEntityExtractor implements CodeEntityExtractor {

    @Override
    public boolean canHandle(String nodeType) {
        return nodeType.equals("call");


    }

    @Override
    public List<CodeEntity> extract(Node node, ExtractionContext context, String sourceFilePath, String sourceCode) {
        List<CodeEntity> calls = new ArrayList<>();
        Node functionNode = node.getChildByFieldName("function");
        String calleeText = extractText(functionNode, sourceCode);
        SourceLocation loc = buildLocation(sourceFilePath, node);

        CallEntity callEntity = new CallEntity.Builder()
                .id(CodeEntity.generateId(loc))
                .name(calleeText)
                .type(CodeEntityType.CALL)
                .location(loc)
                .parentId(context.peekContext() != null
                        ? context.peekContext().getId()
                        : null)
                .build();
        calls.add(callEntity);
        return calls;
    
    }
    

    
}
