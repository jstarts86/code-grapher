package com.jstarts.codegrapher.core;

import com.jstarts.codegrapher.core.entities.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CallGraphResolver {

    private final GlobalSymbolTable symbolTable;

    public CallGraphResolver(GlobalSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void resolveCalls(List<CodeEntity> entities) {
        for (CodeEntity entity : entities) {
            if (entity instanceof CallEntity) {
                resolveCall((CallEntity) entity, entities);
            }
        }
    }

    private void resolveCall(CallEntity call, List<CodeEntity> allEntities) {
        String callee = call.getCallee();
        if (callee == null)
            return;

        // 1. Check for local definition or import in the same file
        Optional<FileEntity> sourceFile = findSourceFile(call, allEntities);
        if (sourceFile.isPresent()) {
            FileEntity file = sourceFile.get();

            // Check imports in this file
            Optional<String> importedId = findImportedId(file, callee, allEntities);
            if (importedId.isPresent()) {
                call.setResolvedFunctionId(importedId.get());
                return;
            }

            // Check definitions in this file (top-level functions/classes)
            // We need to construct the qualified name: module.callee
            String moduleQName = symbolTable.deriveQualifiedName(file);
            if (moduleQName != null) {
                String potentialQName = moduleQName + "." + callee;
                CodeEntity target = symbolTable.lookup(potentialQName);
                if (target != null) {
                    call.setResolvedFunctionId(target.getId());
                    return;
                }
            }
        }

        // 2. Check for global symbols (if not imported, but maybe built-in or
        // implicit?)
        // Or if callee is fully qualified name "mod.func"
        CodeEntity target = symbolTable.lookup(callee);
        if (target != null) {
            call.setResolvedFunctionId(target.getId());
            return;
        }

        // 3. Handle "obj.method()" - simplistic approach
        if (callee.contains(".")) {
            // Try to resolve the prefix
            int lastDot = callee.lastIndexOf('.');
            String prefix = callee.substring(0, lastDot);
            String method = callee.substring(lastDot + 1);

            // If prefix is a class name (e.g. User.method - static method or constructor?)
            // Or if prefix is a module (e.g. os.path.join)

            // Try resolving prefix as a symbol
            // If prefix is "os.path", and we imported "os", we might not have "os.path" in
            // symbol table
            // unless we resolved imports deeply.

            // Let's try resolving prefix in current scope (imports)
            if (sourceFile.isPresent()) {
                Optional<String> prefixId = findImportedId(sourceFile.get(), prefix, allEntities);
                if (prefixId.isPresent()) {
                    // Prefix is imported. e.g. "import utils" -> prefix="utils".
                    // callee="utils.log_info"
                    // The imported ID points to the module/file "utils".
                    // We need to look up "log_info" inside that module.
                    CodeEntity moduleEntity = symbolTable.lookup(symbolTable.deriveQualifiedName(
                            (CodeEntity) allEntities.stream().filter(e -> e.getId().equals(prefixId.get())).findFirst()
                                    .orElse(null)));

                    // Wait, we have the ID. We can get the entity.
                    // Then derive its QName. Then append method.
                    // But we need the entity object.
                    // SymbolTable maps Name -> Entity.
                    // We need ID -> Entity.
                    // We can scan allEntities (slow) or assume SymbolTable has it if we could
                    // lookup by ID.
                    // GlobalSymbolTable only has Name -> Entity.

                    // Optimization: Pass a map of ID->Entity to this resolver?
                    // Or just scan for now.
                }
            }
        }
    }

    private Optional<FileEntity> findSourceFile(CallEntity call, List<CodeEntity> allEntities) {
        String parentId = call.getParentId();
        while (parentId != null) {
            String currentId = parentId;
            Optional<CodeEntity> parent = allEntities.stream().filter(e -> e.getId().equals(currentId)).findFirst();
            if (parent.isPresent()) {
                if (parent.get() instanceof FileEntity) {
                    return Optional.of((FileEntity) parent.get());
                }
                parentId = parent.get().getParentId();
            } else {
                break;
            }
        }
        return Optional.empty();
    }

    private Optional<String> findImportedId(FileEntity file, String name, List<CodeEntity> allEntities) {
        return allEntities.stream()
                .filter(e -> e instanceof ImportEntity && file.getId().equals(e.getParentId()))
                .map(e -> (ImportEntity) e)
                .filter(imp -> imp.getResolvedReferences() != null && imp.getResolvedReferences().containsKey(name))
                .map(imp -> imp.getResolvedReferences().get(name))
                .findFirst();
    }
}
