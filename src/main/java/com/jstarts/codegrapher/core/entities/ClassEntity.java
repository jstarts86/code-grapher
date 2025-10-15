package com.jstarts.codegrapher.core.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ClassEntity.Builder.class)
public class ClassEntity extends CodeEntity {

    private ClassEntity(Builder builder) {
        super(builder);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends CodeEntity.Builder<Builder> {
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
