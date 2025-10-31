package com.jstarts.codegrapher.core.entities;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.ToString;

@JsonDeserialize(builder = ClassEntity.Builder.class)
@ToString
public class ClassEntity extends CodeEntity {
    private final List<String> superClasses;

    private ClassEntity(Builder builder) {
        super(builder);
        this.superClasses = builder.superClasses;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends CodeEntity.Builder<Builder> {
        private List<String> superClasses;

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
