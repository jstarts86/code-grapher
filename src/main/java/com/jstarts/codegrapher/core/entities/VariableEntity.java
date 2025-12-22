package com.jstarts.codegrapher.core.entities;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class VariableEntity extends CodeEntity {
    private final String declaredType;
    private final boolean isTyped;
    private final boolean isAssigned;
    private final boolean isParameterLike;
    private final String typeId;

    protected VariableEntity(Builder builder) {
        super(builder);
        this.declaredType = builder.declaredType;
        this.isTyped = builder.isTyped;
        this.isAssigned = builder.isAssigned;
        this.isParameterLike = builder.isParameterLike;
        this.typeId = builder.typeId;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String declaredType;
        private boolean isTyped;
        private boolean isAssigned;
        private boolean isParameterLike;
        private String typeId;

        public Builder declaredType(String declaredType) {
            this.declaredType = declaredType;
            return this;
        }

        public Builder typeId(String typeId) {
            this.typeId = typeId;
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

        public Builder isParameterLike(boolean isParameterLike) {
            this.isParameterLike = isParameterLike;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public VariableEntity build() {
            super.type(CodeEntityType.VARIABLE);
            return new VariableEntity(this);
        }

    }

}
