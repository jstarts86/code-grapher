package com.jstarts.codegrapher.extractors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.PythonTypeCanon;

import lombok.Getter;

@Getter
public class ExtractionContext {

    private final Deque<CodeEntity> contextStack = new ArrayDeque<>();
    private final List<CodeEntity> allExtractedEntities = new ArrayList<>();
    private final PythonTypeCanon typeCanon = new PythonTypeCanon();

    /**
     * Push a new entity onto the active scope stack
     * and record it globally.
     */
    public void pushContext(CodeEntity codeEntity) {
        if (codeEntity == null) {
            throw new IllegalArgumentException("Cannot push a null CodeEntity onto the context stack.");
        }
        contextStack.push(codeEntity);
        allExtractedEntities.add(codeEntity);
    }

    public void addEntity(CodeEntity e) {
        allExtractedEntities.add(e);
    }

    /**
     * Return the current parent (top of stack) without removing it.
     */
    public CodeEntity peekContext() {
        return contextStack.peek();
    }

    /**
     * Pop the current scope. Returns the entity if present or null if stack empty.
     */
    public CodeEntity popContext() {
        if (contextStack.isEmpty()) {
            return null; // prevents NoSuchElementException
        }
        return contextStack.pop();
    }

    /**
     * All entities encountered during the walk.
     */
    public List<CodeEntity> getAllEntities() {
        return allExtractedEntities;
    }

    /**
     * For debugging or diagnostic logging.
     */
    public int stackDepth() {
        return contextStack.size();
    }

    public boolean isEmpty() {
        return contextStack.isEmpty();
    }

}
