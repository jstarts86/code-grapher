### **Graph Ontology (Schema)**

**Nodes:**

- File
- Module/Package
- Type (Represents Class, Interface, Enum)
- Method
- Field (Class/Interface members)
- LocalVariable
- Parameter
- Annotation

**Edges:**

- CONTAINS (e.g., Package -> File, File -> Type, Type -> Method/Field)
- IMPORTS (File -> Type/Package)
- EXTENDS (Type -> Type)
- IMPLEMENTS (Type -> Type)
- HAS_PARAMETER (Method -> Parameter)
- RETURNS (Method -> Type)
- CALLS (Method -> Method)
- ACCESSES (Method -> Field/LocalVariable/Parameter, with read/write property)
- ANNOTATED_WITH (Any Node -> Annotation)

**Key Attributes (on Nodes/Edges):**

- Location: (filePath, startLine, endLine, startChar, endChar)
- Signature: For methods.
- Scope: (public, private, protected, package-private)
- Modifiers: (static, final, abstract, etc.)
- Type: For variables, parameters, fields, and method returns.
- fullyQualifiedName (FQN): The unique identifier for most nodes.
