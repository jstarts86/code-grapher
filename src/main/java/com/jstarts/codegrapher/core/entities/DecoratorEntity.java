package com.jstarts.codegrapher.core.entities;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DecoratorEntity extends CodeEntity {
    private final String expression;
    private final String targetId;

    protected DecoratorEntity(Builder builder) {
        super(builder);
        this.expression = builder.expression;
        this.targetId = builder.targetId;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {

        private String expression;
        private String targetId;

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public DecoratorEntity build() {
            super.type(CodeEntityType.DECORATOR);
            return new DecoratorEntity(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

    }
}
