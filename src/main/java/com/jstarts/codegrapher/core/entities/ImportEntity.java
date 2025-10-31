package com.jstarts.codegrapher.core.entities;

import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * Represents a single import statement in a Python file.
 *
 * Examples:
 *   import os
 *   import numpy as np
 *   from pathlib import Path, PurePath
 *   from .. import utils
 */
@Getter
public class ImportEntity extends CodeEntity {

    private final String fromModule;              // e.g. "pathlib" or null for plain import
    private final boolean isFromImport;           // true if "from X import Y"
    private final boolean isRelative;             // true if relative import ("from ..foo import bar")
    private final int relativeLevel;              // number of leading dots for relative imports
    private final List<String> importedNames;     // e.g. ["Path", "PurePath"] or ["os"]
    private final Map<String, String> aliases;    // alias -> full name, e.g. { "np" : "numpy" }

    protected ImportEntity(Builder builder) {
        super(builder);
        this.fromModule = builder.fromModule;
        this.isFromImport = builder.isFromImport;
        this.isRelative = builder.isRelative;
        this.relativeLevel = builder.relativeLevel;
        this.importedNames = builder.importedNames;
        this.aliases = builder.aliases;
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

    @Override
    public String toString() {
        String base = isFromImport
                ? String.format("from %s import %s", 
                    fromModule != null ? fromModule : "", 
                    importedNames != null ? importedNames : "")
                : String.format("import %s", importedNames);
        if (aliases != null && !aliases.isEmpty()) {
            base += " as " + aliases;
        }
        return base;
    }
}
