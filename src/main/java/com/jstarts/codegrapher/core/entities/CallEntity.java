package com.jstarts.codegrapher.core.entities;

import lombok.ToString;

@ToString
public class CallEntity extends CodeEntity {
    private final String callee;
    private final int argCount;
    private String resolvedFunctionId;

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String callee;
        private int argCount;
        private String resolvedFunctionId;

        public Builder callee(String callee) {
            this.callee = callee;
            return this;
        }

        public Builder argCount(int argCount) {
            this.argCount = argCount;
            return this;
        }

        public Builder resolvedFunctionId(String resolvedFunctionId) {
            this.resolvedFunctionId = resolvedFunctionId;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public CallEntity build() {
            super.type(CodeEntityType.CALL);
            return new CallEntity(this);
        }
    }

    protected CallEntity(Builder builder) {
        super(builder);
        this.callee = builder.callee;
        this.argCount = builder.argCount;
        this.resolvedFunctionId = builder.resolvedFunctionId;
    }

    public void setResolvedFunctionId(String resolvedFunctionId) {
        this.resolvedFunctionId = resolvedFunctionId;
    }

    public String getCallee() {
        return callee;
    }

    public String getResolvedFunctionId() {
        return resolvedFunctionId;
    }

    public int getArgCount() {
        return argCount;
    }
}
