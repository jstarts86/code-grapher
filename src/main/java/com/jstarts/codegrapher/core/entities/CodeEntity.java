package com.jstarts.codegrapher.core.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

@Getter
public abstract class CodeEntity {
    protected final String id;
    protected final String name;
    protected final CodeEntityType type;
    protected final SourceLocation location;
    protected final String parentId;
    protected final Set<String> modifiers;
    protected final Map<String, Object> attributes;
    protected final String language;
    protected final long version;

    protected CodeEntity(Builder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.location = builder.location;
        this.parentId = builder.parentId;
        this.modifiers = Collections.unmodifiableSet(builder.modifiers);
        this.attributes = Collections.unmodifiableMap(builder.attributes);
        this.language = builder.language;
        this.version = builder.version;
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
        props.put("language", language);
        props.put("version", version);
        props.putAll(attributes);
        return props;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String id;
        private String name;
        private CodeEntityType type;
        private SourceLocation location;
        private String parentId;
        private Set<String> modifiers = new HashSet<>();
        private Map<String, Object> attributes = new HashMap<>();
        private String language = "python";
        private long version = System.currentTimeMillis();

        public T modifiers(Set<String> modifiers) {
            this.modifiers = new HashSet<>(modifiers);
            return self();
        }

        public T attributes(Map<String, Object> attributes) {
            this.attributes = new HashMap<>(attributes);
            return self();
        }

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

        public T language(String language) {
            this.language = language;
            return self();
        }

        public T addModifier(String modifier) {
            this.modifiers.add(modifier);
            return self();
        }

        public T addAttribute(String key, Object val) {
            this.attributes.put(key, val);
            return self();
        }

        public T version(long v) {
            this.version = v;
            return self();
        }

        protected abstract T self();

        public abstract CodeEntity build();
    }

}
