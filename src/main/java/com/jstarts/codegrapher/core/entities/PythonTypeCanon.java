package com.jstarts.codegrapher.core.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PythonTypeCanon {
    private Map<String, PythonTypeEntity> registry = new HashMap<>();

    public PythonTypeEntity getCanonicalType(String signature, Supplier<PythonTypeEntity> creator) {
        if (registry.containsKey(signature)) {
            return registry.get(signature);
        }

        PythonTypeEntity newType = creator.get();
        if (!signature.equals(newType.getSignature())) {
        }
        registry.put(signature, newType);

        return newType;
    }

    public PythonTypeEntity get(String signature) {
        return registry.get(signature);
    }

    public void add(PythonTypeEntity type) {
        registry.put(type.getSignature(), type);
    }

    public boolean containsSignature(String signature) {
        return registry.containsKey(signature);
    }

}
