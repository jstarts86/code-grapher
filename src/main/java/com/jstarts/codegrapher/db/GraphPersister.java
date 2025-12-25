package com.jstarts.codegrapher.db;

import com.jstarts.codegrapher.core.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPersister {

    private final FalkorDBClient client;

    public GraphPersister(FalkorDBClient client) {
        this.client = client;
    }

    public void persist(List<CodeEntity> entities) {
        // 1. Create Nodes
        for (CodeEntity entity : entities) {
            persistNode(entity);
        }

        // 2. Create Relationships
        for (CodeEntity entity : entities) {
            persistRelationships(entity);
        }
    }

    private void persistNode(CodeEntity entity) {
        String label = getLabel(entity);
        Map<String, Object> params = new HashMap<>();
        params.put("id", entity.getId());
        params.put("name", entity.getName());

        if (entity.getLocation() != null) {
            params.put("filePath", entity.getLocation().filePath());
            params.put("startLine", entity.getLocation().startLine());
            params.put("startCol", entity.getLocation().startCol());
            params.put("endLine", entity.getLocation().endLine());
            params.put("endCol", entity.getLocation().endCol());
        }

        StringBuilder query = new StringBuilder();
        query.append(String.format("MERGE (n:%s {id: $id}) ", label));
        query.append("SET n.name = $name ");

        if (entity.getLocation() != null) {
            query.append(", n.filePath = $filePath ");
            query.append(", n.startLine = $startLine ");
            query.append(", n.startCol = $startCol ");
            query.append(", n.endLine = $endLine ");
            query.append(", n.endCol = $endCol ");
        }

        // Type specific properties
        if (entity instanceof FunctionEntity) {
            FunctionEntity f = (FunctionEntity) entity;
            params.put("isAsync", f.isAsync());
            query.append(", n.isAsync = $isAsync ");
            if (f.getReturnType() != null) {
                params.put("returnType", f.getReturnType());
                query.append(", n.returnType = $returnType ");
            }
        } else if (entity instanceof CallEntity) {
            CallEntity c = (CallEntity) entity;
            params.put("callee", c.getCallee());
            params.put("argCount", c.getArgCount());
            query.append(", n.callee = $callee, n.argCount = $argCount ");
        } else if (entity instanceof ImportEntity) {
            ImportEntity i = (ImportEntity) entity;
            if (i.getFromModule() != null) {
                params.put("fromModule", i.getFromModule());
                query.append(", n.fromModule = $fromModule ");
            }
        } else if (entity instanceof PythonTypeEntity) {
            PythonTypeEntity t = (PythonTypeEntity) entity;
            params.put("signature", t.getSignature());
            query.append(", n.signature = $signature ");
        }

        client.executeQuery(query.toString(), params);
    }

    private void persistRelationships(CodeEntity entity) {
        // PARENT_OF
        if (entity.getParentId() != null) {
            String query = "MATCH (p {id: $parentId}), (c {id: $childId}) MERGE (p)-[:PARENT_OF]->(c)";
            Map<String, Object> params = new HashMap<>();
            params.put("parentId", entity.getParentId());
            params.put("childId", entity.getId());
            client.executeQuery(query, params);
        }

        // CALLS
        if (entity instanceof CallEntity) {
            CallEntity c = (CallEntity) entity;
            if (c.getResolvedFunctionId() != null) {
                String query = "MATCH (c:Call {id: $callId}), (f {id: $funcId}) MERGE (c)-[:CALLS]->(f)";
                Map<String, Object> params = new HashMap<>();
                params.put("callId", c.getId());
                params.put("funcId", c.getResolvedFunctionId());
                client.executeQuery(query, params);
            }
        }

        // IMPORTS
        if (entity instanceof ImportEntity) {
            ImportEntity i = (ImportEntity) entity;
            if (i.getResolvedReferences() != null) {
                for (Map.Entry<String, String> entry : i.getResolvedReferences().entrySet()) {
                    String importedName = entry.getKey();
                    String targetId = entry.getValue();

                    String query = "MATCH (i:Import {id: $importId}), (t {id: $targetId}) MERGE (i)-[:IMPORTS {alias: $alias}]->(t)";
                    Map<String, Object> params = new HashMap<>();
                    params.put("importId", i.getId());
                    params.put("targetId", targetId);
                    params.put("alias", importedName);
                    client.executeQuery(query, params);
                }
            }
        }

        // RETURN_TYPE (Function -> Type)
        if (entity instanceof FunctionEntity) {
            FunctionEntity f = (FunctionEntity) entity;
            if (f.getReturnTypeId() != null) {
                String query = "MATCH (f:Function {id: $funcId}), (t:Type {id: $typeId}) MERGE (f)-[:RETURNS]->(t)";
                Map<String, Object> params = new HashMap<>();
                params.put("funcId", f.getId());
                params.put("typeId", f.getReturnTypeId());
                client.executeQuery(query, params);
            }

            // Parameter types
            for (FunctionEntity.Parameter param : f.getParameters()) {
                if (param.getTypeId().isPresent()) {
                    String query = "MATCH (f:Function {id: $funcId}), (t:Type {id: $typeId}) MERGE (f)-[:HAS_PARAM {name: $name}]->(t)";
                    Map<String, Object> params = new HashMap<>();
                    params.put("funcId", f.getId());
                    params.put("typeId", param.getTypeId().get());
                    params.put("name", param.getName());
                    client.executeQuery(query, params);
                }
            }
        }
    }

    private String getLabel(CodeEntity entity) {
        if (entity instanceof FileEntity)
            return "File";
        if (entity instanceof ClassEntity)
            return "Class";
        if (entity instanceof FunctionEntity)
            return "Function";
        if (entity instanceof CallEntity)
            return "Call";
        if (entity instanceof ImportEntity)
            return "Import";
        if (entity instanceof PackageEntity)
            return "Package";
        if (entity instanceof PythonTypeEntity)
            return "Type";
        if (entity instanceof VariableEntity)
            return "Variable";
        return "Entity";
    }
}
