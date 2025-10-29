package com.jstarts.codegrapher.core.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FunctionEntity extends CodeEntity {

    private final boolean isAsync;
    private final List<String> typeParameters;
    private final List<Parameter> parameters;
    private final String returnType;

    protected FunctionEntity(Builder builder) {
        super(builder);
        this.isAsync = builder.isAsync;
        this.returnType = builder.returnType;
        this.parameters = builder.parameters;
        this.typeParameters = builder.typeParameters;
    }

    @Getter
    @AllArgsConstructor
    public static class Parameter {
        private final String name;
        private final String typeAnnotation;
        private final String defaultValue;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private boolean isAsync;
        private String returnType;
        private List<Parameter> parameters;
        private List<String> typeParameters;

        public Builder isAsync(Boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder parameters(List<Parameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder typeParameters(List<String> typeParameters) {
            this.typeParameters = typeParameters;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FunctionEntity build() {
            super.type(CodeEntityType.FUNCTION);
            return new FunctionEntity(this);
        }

    }

}
