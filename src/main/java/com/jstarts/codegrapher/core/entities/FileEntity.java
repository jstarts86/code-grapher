package com.jstarts.codegrapher.core.entities;

import java.util.Map;

public class FileEntity extends CodeEntity {
    private final String moduleName;

    protected FileEntity(Builder builder) {
        super(builder);
        this.moduleName = builder.moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Map<String, Object> toProperties() {
        Map<String, Object> props = super.toProperties();
        props.put("module_name", moduleName);
        return props;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String moduleName;

        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FileEntity build() {
            super.type(CodeEntityType.FILE);
            return new FileEntity(this);

        }
    }

}
