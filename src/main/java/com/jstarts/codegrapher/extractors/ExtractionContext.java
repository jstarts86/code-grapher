package com.jstarts.codegrapher.extractors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.jstarts.codegrapher.core.entities.CodeEntity;

import lombok.Getter;

@Getter
public class ExtractionContext {
    private final Deque<CodeEntity> contextStack = new ArrayDeque<>();
    private List<CodeEntity> allExtractedEntities = new ArrayList<>();

    public void pushContext(CodeEntity codeEntity) {
        contextStack.push(codeEntity);
        allExtractedEntities.add(codeEntity);  // <-- STORE IT
    }

    public CodeEntity peekContext() {
        return contextStack.peek();
    }

    public CodeEntity popContext() {
        return contextStack.pop();
    }

}
