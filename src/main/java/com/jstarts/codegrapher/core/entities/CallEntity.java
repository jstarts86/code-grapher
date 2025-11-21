package com.jstarts.codegrapher.core.entities;

import lombok.ToString;

@ToString
public class CallEntity extends CodeEntity {
    private final String callee;
    private final int argCount;

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String callee;
        private int argCount;

        public Builder callee(String callee) { this.callee = callee; return this; }
        public Builder argCount(int argCount) { this.argCount = argCount; return this; }

        @Override protected Builder self() { return this; }

        @Override public CallEntity build() {
            super.type(CodeEntityType.CALL);
            return new CallEntity(this);
        }
    }

    protected CallEntity(Builder builder) {
        super(builder);
        this.callee = builder.callee;
        this.argCount = builder.argCount;
    }
}
