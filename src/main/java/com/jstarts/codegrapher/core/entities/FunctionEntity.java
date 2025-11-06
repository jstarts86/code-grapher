package com.jstarts.codegrapher.core.entities;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
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
    @ToString
    public static class Parameter {
        private final ParameterKind kind;
        private final String name;
        private final Optional<String> typeAnnotation;
        private final Optional<String> defaultValue;
    }

    public enum ParameterKind {
        NORMAL,
        TYPED,
        DEFAULT,
        TYPED_DEFAULT,
        LIST_SPLAT,
        DICT_SPLAT,
        POSITIONAL_SEPARATOR,
        KEYWORD_SEPARATOR
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private boolean isAsync;
        private String returnType;
        private List<Parameter> parameters;
        private List<String> typeParameters;

        public Builder isAsync(boolean isAsync) {
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
