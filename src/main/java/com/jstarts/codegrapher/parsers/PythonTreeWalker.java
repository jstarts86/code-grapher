package com.jstarts.codegrapher.parsers;

import java.util.Optional;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.extractors.ClassEntityExtractor;
import com.jstarts.codegrapher.extractors.CodeEntityExtractor;
import com.jstarts.codegrapher.extractors.ExtractionContext;
import com.jstarts.codegrapher.extractors.ExtractorRegistry;

import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;
import ch.usi.si.seart.treesitter.TreeCursor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PythonTreeWalker {
    private final ExtractorRegistry registry;
    private final ExtractionContext context;

    private final String sourceFilePath;
    private final String sourceCode;

    public void walk(Node node) {

        if (node == null) {
            return;
        }
        Optional<CodeEntity> extracted = registry.getExtractor(node.getType())
                .flatMap(extractor -> extractor.extract(node, context, sourceFilePath, sourceCode));
        if (extracted.isPresent()) {
            CodeEntity entity = extracted.get();
            context.pushContext(entity);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i));
        }
        if (extracted.isPresent()) {
            context.popContext();
        }
    }

}
