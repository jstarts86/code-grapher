package com.jstarts.codegrapher.extractors;

import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;

import ch.usi.si.seart.treesitter.Node;

public class ClassEntityExtraction implements CodeEntityExtractor {


    @Override
    public boolean canHandle(String nodeType) {
        if(nodeType.equals("class_definition")) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Optional<CodeEntity> extract(Node node, ExtractionContext context) {
        Node nameNode = node.getChildByFieldName("name");
        return null;
    }



    
}
