package com.jstarts.codegrapher.extractors;

import java.util.ArrayDeque;
import java.util.Deque;

import com.jstarts.codegrapher.core.entities.CodeEntity;

public class ExtractionContext {
    private final Deque<CodeEntity> contextStack = new ArrayDeque<>();

    public void pushContext(CodeEntity codeEntity) {
        contextStack.push(codeEntity);
    }

    public CodeEntity peekContext() {
        return contextStack.peek();
    }

    public CodeEntity popContext() {
        return contextStack.pop();
    }


}
