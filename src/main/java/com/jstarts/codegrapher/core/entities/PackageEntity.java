package com.jstarts.codegrapher.core.entities;

import java.util.Map;
import lombok.ToString;

@ToString
public class PackageEntity extends CodeEntity {

    protected PackageEntity(Builder builder) {
        super(builder);
    }

    public static class Builder extends CodeEntity.Builder<Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public PackageEntity build() {
            super.type(CodeEntityType.PACKAGE);
            return new PackageEntity(this);
        }
    }
}
