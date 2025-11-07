package com.jstarts.codegrapher.core.entities;

import lombok.Getter;

@Getter
public class FieldEntity extends CodeEntity {
    private final String declaredType;
    private final boolean isTyped;
    private final boolean isAssigned;
    private final boolean isClassVariable;

    protected FieldEntity(Builder builder) {
        super(builder);
        this.declaredType = builder.declaredType;
        this.isTyped = builder.isTyped;
        this.isAssigned = builder.isAssigned;
        this.isClassVariable = builder.isClassVariable;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String declaredType;
        private boolean isTyped;
        private boolean isAssigned;
        private boolean isClassVariable;

        public Builder declaredType(String declaredType) {
            this.declaredType = declaredType;
            return this;
        }

        public Builder isTyped(boolean isTyped) {
            this.isTyped = isTyped;
            return this;
        }

        public Builder isAssigned(boolean isAssigned) {
            this.isAssigned = isAssigned;
            return this;
        }

        public Builder isClassVariable(boolean isClassVariable) {
            this.isClassVariable = isClassVariable;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FieldEntity build() {
            super.type(CodeEntityType.FIELD);
            return new FieldEntity(this);
        }

    }
    
}
