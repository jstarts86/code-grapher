package com.jstarts.codegrapher.core.entities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Getter;

@Getter
public class PythonTypeEntity extends CodeEntity {
    private final String signature;
    private final List<PythonTypeEntity> generics;

    private PythonTypeEntity(Builder builder) {
        super(builder);
        this.signature = builder.signature;
        this.generics = builder.generics;
    }

    @Override
    public Map<String, Object> toProperties() {
        Map<String, Object> props = super.toProperties();
        props.put("signature", signature);
        if (generics != null && !generics.isEmpty()) {
            props.put("generics", generics.stream()
                    .map(PythonTypeEntity::getSignature)
                    .collect(Collectors.toList()));
        }
        return props;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String signature;
        private List<PythonTypeEntity> generics;

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder generics(List<PythonTypeEntity> generics) {
            this.generics = generics;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public PythonTypeEntity build() {
            // If ID is not set, generate it from the signature
            if (this.signature != null) {
                this.id(DigestUtils.sha256Hex(this.signature).substring(0, 16));
            }
            // Ensure type is set to TYPE
            this.type(CodeEntityType.TYPE);
            return new PythonTypeEntity(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        PythonTypeEntity that = (PythonTypeEntity) o;
        return Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), signature);
    }
}
