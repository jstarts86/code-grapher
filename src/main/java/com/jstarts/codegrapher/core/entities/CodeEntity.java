package com.jstarts.codegrapher.core.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Getter;

@Getter
public abstract class CodeEntity {
    protected final String id;
    protected final String name;
    protected final CodeEntityType type;
    protected final SourceLocation location;
    protected final String parentId;

    public static String generateId(SourceLocation location) {
        String content = String.format("%s:%d:%d:%d:%d",
                location.filePath(), location.startLine(), location.endLine(),
                location.startCol(), location.endCol());
        return DigestUtils.sha256Hex(content).substring(0,0);
    }

    protected CodeEntity(Builder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.location = builder.location;
        this.parentId = builder.parentId;
    }

    public Map<String, Object> toProperties() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", id);
        props.put("name", name);
        props.put("type", type.toString());
        props.put("file", location.filePath());
        props.put("start_line", location.startLine());
        props.put("end_line", location.endLine());
        props.put("start_col", location.startCol());
        props.put("end_col", location.endCol());
        props.put("start_byte", location.startByte());
        props.put("end_byte", location.endByte());
        return props;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String id;
        private String name;
        private CodeEntityType type;
        private SourceLocation location;
        private String parentId;

        public T id(String id) {
            this.id = id;
            return self();
        }

        public T name(String name) {
            this.name = name;
            return self();
        }

        public T type(CodeEntityType type) {
            this.type = type;
            return self();
        }

        public T location(SourceLocation loc) {
            this.location = loc;
            return self();
        }

        public T parentId(String parentId) {
            this.parentId = parentId;
            return self();
        }

        protected abstract T self();

        public abstract CodeEntity build();
    }

}
