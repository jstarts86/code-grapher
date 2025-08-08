# Graph Ontology Implementation Progress

This document tracks the implementation status of the features defined in `GraphOntology.md`.

## Nodes

- [x] File (`FileDef.java`)
- [x] Package (`PackageDef.java`)
- [x] Type (`TypeDef.java`)
  - [x] Class (`ClassDef.java`)
  - [x] Interface (`InterfaceDef.java`)
  - [ ] Enum
- [x] Method (`MethodDef.java`)
- [x] Field (`FieldDef.java`)
- [ ] LocalVariable
- [ ] Parameter
- [x] Annotation (`AnnotationDef.java`)

## Edges & Relationships

- **CONTAINS**
  - [x] `Package` -> `File`
  - [x] `Type` -> `Method`
  - [x] `Type` -> `Field`
- **IMPORTS**
  - [ ] `File` -> `Type`
  - [ ] `File` -> `Package`
- **EXTENDS**
  - [ ] `Type` -> `Type`
- **IMPLEMENTS**
  - [ ] `Type` -> `Type`
- **HAS_PARAMETER**
  - [ ] `Method` -> `Parameter`
- **RETURNS**
  - [x] `Method` -> `Type`
- **CALLS**
  - [ ] `Method` -> `Method`
- **ACCESSES**
  - [ ] `Method` -> `Field`
  - [ ] `Method` -> `LocalVariable`
  - [ ] `Method` -> `Parameter`
- **ANNOTATED_WITH**
  - [x] `Any Node` -> `Annotation`

## Key Attributes (on Nodes/Edges)

- [ ] **Location**
- [ ] **Signature**
- [x] **Scope**
- [x] **Modifiers**
- [x] **Type**
- [ ] **fullyQualifiedName (FQN)**
