package com.jstarts.codegrapher.core.entities;

import java.util.Map;

public record PythonType(

    String name,
    String module,
    boolean isBuiltin,
    boolean isCollection,
    Map<String, PythonType> generis
) {
    public static PythonType of(String name) {
        return new PythonType(name, null, true, false, Map.of());
    }
}
