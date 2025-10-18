package com.jstarts.codegrapher.core.entities;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ClassEntity.Builder.class)
public class ClassEntity extends CodeEntity {
    private final List<String> superClasses;
    private final List<String> types;

    private ClassEntity(Builder builder) {
        super(builder);
        this.superClasses = builder.superClasses;
        this.types = builder.types;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends CodeEntity.Builder<Builder> {
        private List<String> superClasses;
        private List<String> types;

        public Builder types(List<String> types) {
            this.types = types;
            return this;
        }

        public Builder superClasses(List<String> superClasses) {
            this.superClasses = superClasses;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ClassEntity build() {
            super.type(CodeEntityType.CLASS);
            return new ClassEntity(this);
        }

    }

}
