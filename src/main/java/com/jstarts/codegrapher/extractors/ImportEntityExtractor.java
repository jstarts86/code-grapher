package com.jstarts.codegrapher.extractors;

import ch.usi.si.seart.treesitter.Node;
import com.jstarts.codegrapher.core.entities.CodeEntity;
import com.jstarts.codegrapher.core.entities.CodeEntityType;
import com.jstarts.codegrapher.core.entities.ImportEntity;
import com.jstarts.codegrapher.core.entities.SourceLocation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImportEntityExtractor implements CodeEntityExtractor {

    private static final String IMPORT_STATEMENT = "import_statement";
    private static final String IMPORT_FROM_STATEMENT = "import_from_statement";

    @Override
    public boolean canHandle(String nodeType) {
        return IMPORT_STATEMENT.equals(nodeType) || IMPORT_FROM_STATEMENT.equals(nodeType);
    }

    @Override
    public List<CodeEntity> extract(Node node, ExtractionContext context, String sourceFilePath, String sourceCode) {
        return buildImportEntity(node, context, sourceFilePath, sourceCode)
                .map(entity -> (CodeEntity) entity)
                .map(List::of)
                .orElse(List.of());
    }

    private Optional<ImportEntity> buildImportEntity(
            Node node,
            ExtractionContext context,
            String sourceFilePath,
            String sourceCode
    ) {
        try {
            boolean isFromImport = IMPORT_FROM_STATEMENT.equals(node.getType());
            
            ModuleInfo moduleInfo = isFromImport 
                ? extractModuleInfo(node, sourceCode)
                : ModuleInfo.empty();
            
            ImportItems items = extractImportItems(node, sourceCode);
            SourceLocation location = buildLocation(sourceFilePath, node);
            String displayName = buildDisplayName(isFromImport, moduleInfo, items);
            
            return Optional.of(new ImportEntity.Builder()
                    .id(CodeEntity.generateId(location))
                    .name(displayName)
                    .type(CodeEntityType.IMPORT)
                    .location(location)
                    .parentId(Optional.ofNullable(context.peekContext())
                            .map(CodeEntity::getId)
                            .orElse(null))
                    .fromModule(moduleInfo.module)
                    .isFromImport(isFromImport)
                    .isRelative(moduleInfo.isRelative)
                    .relativeLevel(moduleInfo.relativeLevel)
                    .importedNames(items.names)
                    .aliases(items.aliases)
                    .build());
                    
        } catch (Exception e) {
            System.err.println("Failed to extract import from " + sourceFilePath + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // Immutable data holders
    
    private record ModuleInfo(String module, boolean isRelative, int relativeLevel) {
        static ModuleInfo empty() {
            return new ModuleInfo(null, false, 0);
        }
    }
    
    private record ImportItems(List<String> names, Map<String, String> aliases) {
        ImportItems(List<String> names, Map<String, String> aliases) {
            this.names = List.copyOf(names);
            this.aliases = Map.copyOf(aliases);
        }
    }
    
    private record ImportItem(String name, Optional<String> alias) {
        static ImportItem simple(String name) {
            return new ImportItem(name, Optional.empty());
        }
        
        static ImportItem aliased(String name, String alias) {
            return new ImportItem(name, Optional.of(alias));
        }
    }

    // Pure Extraction Functions

    private ModuleInfo extractModuleInfo(Node node, String sourceCode) {
        return extractField(node, "module_name", sourceCode)
                .map(rawModule -> {
                    if (rawModule.startsWith(".")) {
                        int level = countLeadingDots(rawModule);
                        String module = rawModule.substring(level);
                        return new ModuleInfo(
                            module.isEmpty() ? null : module,
                            true,
                            level
                        );
                    }
                    return new ModuleInfo(rawModule, false, 0);
                })
                .orElse(ModuleInfo.empty());
    }

    private ImportItems extractImportItems(Node node, String sourceCode) {
        List<ImportItem> items = streamNamedChildren(node)
                .flatMap(child -> processImportItem(child, sourceCode))
                .toList();
        
        List<String> names = items.stream()
                .map(item -> item.alias().orElse(item.name()))
                .toList();
        
        Map<String, String> aliases = items.stream()
                .filter(item -> item.alias().isPresent())
                .collect(Collectors.toMap(
                    item -> item.alias().get(),
                    ImportItem::name,
                    (a, b) -> a,
                    LinkedHashMap::new
                ));
        
        return new ImportItems(names, aliases);
    }

    private Stream<ImportItem> processImportItem(Node node, String sourceCode) {
        return switch (node.getType()) {
            case "dotted_name", "identifier" -> 
                Stream.of(ImportItem.simple(extractText(node, sourceCode)));
                
            case "aliased_import" -> 
                handleAliasedImport(node, sourceCode);
                
            case "wildcard_import" -> 
                Stream.of(ImportItem.simple("*"));
                
            default -> 
                streamNamedChildren(node)
                    .flatMap(child -> processImportItem(child, sourceCode));
        };
    }

    private Stream<ImportItem> handleAliasedImport(Node node, String sourceCode) {
        return Optional.ofNullable(node.getNamedChild(0))
                .flatMap(nameNode -> Optional.ofNullable(node.getNamedChild(1))
                    .map(aliasNode -> ImportItem.aliased(
                        extractText(nameNode, sourceCode),
                        extractText(aliasNode, sourceCode)
                    )))
                .stream();
    }

    private Stream<Node> streamNamedChildren(Node node) {
        return IntStream.range(0, node.getNamedChildCount())
                .mapToObj(node::getNamedChild);
    }

    private int countLeadingDots(String s) {
        return (int) s.chars()
                .takeWhile(c -> c == '.')
                .count();
    }

    private String buildDisplayName(
            boolean isFromImport,
            ModuleInfo moduleInfo,
            ImportItems items
    ) {
        if (isFromImport) {
            String moduleStr = Optional.ofNullable(moduleInfo.module()).orElse("...");
            return String.format("from %s import %s", moduleStr, String.join(", ", items.names()));
        }
        
        String importList = items.names().stream()
                .map(name -> items.aliases().containsKey(name)
                    ? items.aliases().get(name) + " as " + name
                    : name)
                .collect(Collectors.joining(", "));
                
        return "import " + importList;
    }
}
