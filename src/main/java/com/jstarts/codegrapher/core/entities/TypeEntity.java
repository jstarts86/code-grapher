package com.jstarts.codegrapher.core.entities;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TypeEntity extends CodeEntity {

    private final String typeName;
    private final String module;
    private final boolean isBuiltin;
    private final boolean isCollection;

    protected TypeEntity(Builder builder) {
        super(builder);
        this.typeName = builder.typeName;
        this.module = builder.module;
        this.isBuiltin = builder.isBuiltin;
        this.isCollection = builder.isCollection;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String typeName;
        private String module;
        private boolean isBuiltin;
        private boolean isCollection;

        public Builder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder isBuiltin(boolean builtin) {
            this.isBuiltin = builtin;
            return this;
        }

        public Builder isCollection(boolean collection) {
            this.isCollection = collection;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public TypeEntity build() {
            super.type(CodeEntityType.TYPE);
            return new TypeEntity(this);
        }
    }
}
