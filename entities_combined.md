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
package com.jstarts.codegrapher.core.entities;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.ToString;

@JsonDeserialize(builder = ClassEntity.Builder.class)
@ToString
public class ClassEntity extends CodeEntity {
    private final List<String> superClasses;

    private ClassEntity(Builder builder) {
        super(builder);
        this.superClasses = builder.superClasses;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends CodeEntity.Builder<Builder> {
        private List<String> superClasses;

        public Builder superClasses(List<String> superClasses) {
            this.superClasses = superClasses;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ClassEntity build() {
            super.type(CodeEntityType.CLASS);
            return new ClassEntity(this);
        }

    }

}
package com.jstarts.codegrapher.core.entities;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Data;

@Data
public abstract class CodeEntity {
    protected final String id;
    protected final String name;
    protected final CodeEntityType type;
    protected final SourceLocation location;
    protected final String parentId;

    public static String generateId(SourceLocation location) {
        String content = String.format("%s:%d:%d:%d:%d",
                location.filePath(), location.startLine(), location.endLine(),
                location.startCol(), location.endCol());
        return DigestUtils.sha256Hex(content).substring(0, 16);
    }

    protected CodeEntity(Builder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.location = builder.location;
        this.parentId = builder.parentId;
    }

    public Map<String, Object> toProperties() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", id);
        props.put("name", name);
        props.put("type", type.toString());
        props.put("file", location.filePath());
        props.put("start_line", location.startLine());
        props.put("end_line", location.endLine());
        props.put("start_col", location.startCol());
        props.put("end_col", location.endCol());
        props.put("start_byte", location.startByte());
        props.put("end_byte", location.endByte());
        return props;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String id;
        private String name;
        private CodeEntityType type;
        private SourceLocation location;
        private String parentId;

        public T id(String id) {
            this.id = id;
            return self();
        }

        public T name(String name) {
            this.name = name;
            return self();
        }

        public T type(CodeEntityType type) {
            this.type = type;
            return self();
        }

        public T location(SourceLocation loc) {
            this.location = loc;
            return self();
        }

        public T parentId(String parentId) {
            this.parentId = parentId;
            return self();
        }

        protected abstract T self();

        public abstract CodeEntity build();
    }

}
package com.jstarts.codegrapher.core.entities;

public enum CodeEntityType {
    FILE,
    MODULE,
    PACKAGE,
    CLASS,
    FUNCTION,
    METHOD,
    VARIABLE,
    PARAMETER,
    FIELD,
    IMPORT,
    DECORATOR,
    LAMBDA,
    ATTRIBUTE,
    ASSIGNMENT,
    ARGUMENT,
    TYPE,
    CALL
}
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
package com.jstarts.codegrapher.core.entities;

import java.util.Map;

import lombok.ToString;

@ToString
public class FileEntity extends CodeEntity {
    private final String moduleName;

    protected FileEntity(Builder builder) {
        super(builder);
        this.moduleName = builder.moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Map<String, Object> toProperties() {
        Map<String, Object> props = super.toProperties();
        props.put("module_name", moduleName);
        return props;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String moduleName;

        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FileEntity build() {
            super.type(CodeEntityType.FILE);
            return new FileEntity(this);

        }
    }

}
package com.jstarts.codegrapher.core.entities;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class FunctionEntity extends CodeEntity {

    private final boolean isAsync;
    private final List<String> typeParameters;
    private final List<Parameter> parameters;
    private final String returnType;

    private final String returnTypeId;

    protected FunctionEntity(Builder builder) {
        super(builder);
        this.isAsync = builder.isAsync;
        this.returnType = builder.returnType;
        this.parameters = builder.parameters;
        this.typeParameters = builder.typeParameters;
        this.returnTypeId = builder.returnTypeId;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class Parameter {
        private final ParameterKind kind;
        private final String name;
        private final Optional<String> typeAnnotation;
        private final Optional<String> defaultValue;
        private final Optional<String> typeId;
    }

    public enum ParameterKind {
        NORMAL,
        TYPED,
        DEFAULT,
        TYPED_DEFAULT,
        LIST_SPLAT,
        DICT_SPLAT,
        POSITIONAL_SEPARATOR,
        KEYWORD_SEPARATOR
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private boolean isAsync;
        private String returnType;
        private List<Parameter> parameters;
        private List<String> typeParameters;

        private String returnTypeId;

        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder returnTypeId(String returnTypeId) {
            this.returnTypeId = returnTypeId;
            return this;
        }

        public Builder parameters(List<Parameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder typeParameters(List<String> typeParameters) {
            this.typeParameters = typeParameters;
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
package com.jstarts.codegrapher.core.entities;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;

/**
 * Represents a single import statement in a Python file.
 *
 * Examples:
 * import os
 * import numpy as np
 * from pathlib import Path, PurePath
 * from .. import utils
 */
@Getter
@ToString
public class ImportEntity extends CodeEntity {

    private final String fromModule; // e.g. "pathlib" or null for plain import
    private final boolean isFromImport; // true if "from X import Y"
    private final boolean isRelative; // true if relative import ("from ..foo import bar")
    private final int relativeLevel; // number of leading dots for relative imports
    private final List<String> importedNames; // e.g. ["Path", "PurePath"] or ["os"]
    private final Map<String, String> aliases; // alias -> full name, e.g. { "np" : "numpy" }
    private Map<String, String> resolvedReferences; // importedName -> targetEntityId

    public void setResolvedReferences(Map<String, String> resolvedReferences) {
        this.resolvedReferences = resolvedReferences;
    }

    protected ImportEntity(Builder builder) {
        super(builder);
        this.fromModule = builder.fromModule;
        this.isFromImport = builder.isFromImport;
        this.isRelative = builder.isRelative;
        this.relativeLevel = builder.relativeLevel;
        this.importedNames = builder.importedNames;
        this.aliases = builder.aliases;
        this.resolvedReferences = builder.resolvedReferences;
    }

    /**
     * Builder pattern consistent with other CodeEntity subclasses.
     */
    public static class Builder extends CodeEntity.Builder<Builder> {
        private String fromModule;
        private boolean isFromImport;
        private boolean isRelative;
        private int relativeLevel;
        private List<String> importedNames;
        private Map<String, String> aliases;
        private Map<String, String> resolvedReferences;

        public Builder fromModule(String fromModule) {
            this.fromModule = fromModule;
            return this;
        }

        public Builder isFromImport(boolean isFromImport) {
            this.isFromImport = isFromImport;
            return this;
        }

        public Builder isRelative(boolean isRelative) {
            this.isRelative = isRelative;
            return this;
        }

        public Builder relativeLevel(int relativeLevel) {
            this.relativeLevel = relativeLevel;
            return this;
        }

        public Builder importedNames(List<String> importedNames) {
            this.importedNames = importedNames;
            return this;
        }

        public Builder aliases(Map<String, String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder resolvedReferences(Map<String, String> resolvedReferences) {
            this.resolvedReferences = resolvedReferences;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ImportEntity build() {
            super.type(CodeEntityType.IMPORT);
            return new ImportEntity(this);
        }
    }

    // @Override
    // public String toString() {
    // String base = isFromImport
    // ? String.format("from %s import %s",
    // fromModule != null ? fromModule : "",
    // importedNames != null ? importedNames : "")
    // : String.format("import %s", importedNames);
    // if (aliases != null && !aliases.isEmpty()) {
    // base += " as " + aliases;
    // }
    // return base;
    // }
}
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
package com.jstarts.codegrapher.core.entities;

import lombok.Builder;

@Builder
public record SourceLocation(
        String filePath,
        int startLine,
        int endLine,
        int startCol,
        int endCol,
        int startByte,
        int endByte) {
}
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

    private final ScopeKind scope;

    public enum ScopeKind {
        GLOBAL,
        LOCAL,
        CLASS_FIELD,
        INSTANCE_FIELD
    }

    protected VariableEntity(Builder builder) {
        super(builder);
        this.declaredType = builder.declaredType;
        this.isTyped = builder.isTyped;
        this.isAssigned = builder.isAssigned;
        this.isParameterLike = builder.isParameterLike;
        this.typeId = builder.typeId;
        this.scope = builder.scope;
    }

    public static class Builder extends CodeEntity.Builder<Builder> {
        private String declaredType;
        private boolean isTyped;
        private boolean isAssigned;
        private boolean isParameterLike;
        private String typeId;
        private ScopeKind scope;

        public Builder scope(ScopeKind scope) {
            this.scope = scope;
            return this;
        }

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
