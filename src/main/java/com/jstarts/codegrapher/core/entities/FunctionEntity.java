package com.jstarts.codegrapher.core.entities;

import java.util.List;

public class FunctionEntity extends CodeEntity {

    private final List<String> typeParameters;
    private final boolean isAsync;
    private final String returnType;

    protected FunctionEntity(Builder builder) {
        super(builder);
        this.typeParameters = builder.typeParameters;
        this.isAsync = builder.isAsync;
        this.returnType = builder.returnType;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private List<String> typeParameters;
        private boolean isAsync;
        private String returnType;

        public Builder parameters(List<String> typeParameters) {
            this.typeParameters = typeParameters;
            return this;
        }

        public Builder isAsync(Boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }

        public Builder returnType(boolean isAsync) {
            this.isAsync = isAsync;
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
