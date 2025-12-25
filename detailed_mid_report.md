# Code Grapher: Comprehensive Progress Report
**Date:** December 1, 2025  
**Version:** 2.0 (Detailed Technical Review)  
**Status:** Active Development - Layer 1 (Foundational Graph) Near Completion

---

## 1. Project Overview

### 1.1 Motivation: The Multi-Hunk Problem
Large Language Models (LLMs) struggle with repository-level tasks due to the **"Multi-Hunk Problem"**: bugs or features often span multiple non-contiguous files and functions. Standard RAG (Retrieval Augmented Generation) retrieves text chunks based on similarity, often missing the *structural* context (e.g., "Where is this variable defined?", "What classes inherit from this base class?").

**Code Grapher** solves this by building a **Code Knowledge Graph (CKG)** that maps the repository's structure and semantics. This allows an agent to "walk" the graph to find relevant context, rather than guessing keywords.

### 1.2 The Solution: GraphRAG Architecture
The system is built in layers:
*   **Layer 0: Syntax (AST)**: Raw parsing using **Tree-sitter**.
*   **Layer 1: Semantics (Symbol Graph)**: Entities (Classes, Functions, Variables) and their relationships (Inheritance, Calls, Usage). **<-- CURRENT FOCUS**
*   **Layer 2: Behavior (Flow Graph)**: Control Flow (CFG) and Data Flow (DDG) for deep analysis.

---

## 2. Technology Stack

The system is built on a high-performance, strongly-typed foundation to ensure scalability and correctness.

### Core Technologies
*   **Language**: **Java 21** (Core Logic, Extraction Engine)
    *   *Why*: Strong typing, performance, and robust ecosystem for graph algorithms.
*   **Parsing**: **Tree-sitter** (via `ch.usi.si.seart:java-tree-sitter`)
    *   *Why*: Incremental parsing, error tolerance, and multi-language support (Python, Java, C++, etc.).
*   **Database**: **FalkorDB** (Graph Database)
    *   *Why*: Low-latency Cypher query execution, Redis-based architecture for speed.

### Libraries & Tools
*   **Lombok**: Reduces boilerplate code for Entity classes (Builders, Getters/Setters).
*   **JUnit 5**: Comprehensive unit testing framework.
*   **Gradle/Maven**: Dependency management and build automation.
*   **Docker**: Containerization for the FalkorDB instance.

---

## 3. Technical Architecture & Implementation Details

The core of the system is a Java-based extraction engine that processes Python code.

### 2.1 The Extractor Pattern
We employ a modular **Extractor Pattern** to handle different AST node types.
*   **Interface**: `CodeEntityExtractor`
*   **Dispatch**: An `ExtractorRegistry` delegates nodes to the correct extractor based on `canHandle(nodeType)`.
*   **Context**: An `ExtractionContext` stack tracks the current scope (e.g., inside a Class, inside a Function) to correctly assign `parentId`.

### 2.2 Type System Implementation (New Feature)
*Status: Implemented & Integrated (Nov 27 - Dec 1)*

A robust type system is critical for resolving method signatures and variable usage. We have moved beyond storing types as strings to storing them as graph nodes.

#### Components:
1.  **`PythonTypeParser`**: A recursive descent parser for Python type hints.
    *   **Capabilities**:
        *   **Generics**: Parses `List[int]`, `Dict[str, Any]`.
        *   **Unions**: Flattens `Union[A, B]` and `A | B`.
        *   **Literals**: Handles `Literal['read', 'write']`.
    *   **Normalization**: Ensures `list` and `List` map to the same concept.

2.  **`TypeCanon` (Canonicalization Registry)**:
    *   **Problem**: If 50 functions return `int`, we don't want 50 `int` nodes.
    *   **Solution**: `TypeCanon` maintains a registry of types. It ensures that logically identical types (same signature) resolve to the **same single `PythonTypeEntity` object**. This significantly reduces graph noise and enables efficient "Find Usages" queries.

### 2.3 Entity Extraction Status

| Entity Type | Extractor Class | Status | Technical Details |
| :--- | :--- | :--- | :--- |
| **File** | `FileEntityExtractor` | ✅ Done | Maps file paths and module hierarchy. |
| **Import** | `ImportEntityExtractor` | ✅ Done | • Handles `import x`, `from x import y`, `import x as z`.<br>• Resolves relative imports (e.g., `from ..utils import log`).<br>• Captures aliases for correct symbol resolution. |
| **Class** | `ClassEntityExtractor` | ✅ Done | Captures base classes for inheritance graphs. |
| **Function** | `FunctionEntityExtractor` | ✅ Done | • **Parameters**: Distinguishes `NORMAL`, `TYPED`, `DEFAULT`, `ARGS` (`*args`), `KWARGS` (`**kwargs`).<br>• **Async**: Detects `async def`.<br>• **Return Type**: Links to `PythonTypeEntity`. |
| **Field** | `FieldEntityExtractor` | ✅ Done | Extracts class attributes (e.g., `self.x = 1`). |
| **Variable** | `VariableEntityExtractor` | 🚧 In Progress | • **Current**: Handles assignments (`x = 1`) and typed assignments (`x: int = 1`).<br>• **Logic**: Recursively unpacks tuples (`x, (y, z) = ...`).<br>• **Limitation**: Scope resolution needs refinement to distinguish local vs. global vs. closure variables. |

---

## 4. Graph Schema (Layer 1)

The following nodes and relationships are currently supported or planned for the immediate release.

### Nodes
*   `(:File {path, moduleName})`
*   `(:Class {name, qname})`
*   `(:Function {name, signature, isAsync})`
*   `(:Parameter {name, index, kind})`
*   `(:Type {signature, name})`
*   `(:Variable {name, isTyped})`
*   `(:Import {module, alias})`

### Relationships
*   `(:Class)-[:DEFINED_IN]->(:File)`
*   `(:Function)-[:DEFINED_IN]->(:File/Class)`
*   `(:Class)-[:INHERITS_FROM]->(:Type)`
*   `(:Function)-[:HAS_PARAMETER]->(:Parameter)`
*   `(:Function)-[:RETURNS]->(:Type)`
*   `(:Parameter)-[:HAS_TYPE]->(:Type)`
*   `(:Variable)-[:HAS_TYPE]->(:Type)`
*   `(:File)-[:IMPORTS]->(:Import)`

---

## 5. Roadmap & Next Steps

### Phase 1: Completion of Layer 1 (Dec 1 - Dec 15)
1.  **Variable Scoping**: Refine `VariableEntityExtractor` to correctly attach variables to their scope (Function vs Module).
2.  **Call Graph**: Implement `CallExtractor`.
    *   *Goal*: Create `(:Function)-[:CALLS]->(:Function)` edges.
    *   *Challenge*: Python is dynamic. We will start with static resolution (name matching + import resolution) and mark ambiguous calls.
3.  **Import Resolution**: Build the `ImportResolver` service.
    *   *Goal*: Connect `(:Import)` entities to the actual `(:File)` or `(:Class)` they refer to. This "stitches" the graph together across files.

### Phase 2: Layer 2 - Flow Analysis (Jan 2026)
*   **Control Flow**: Generate CFGs for functions.
*   **Data Flow**: Track variable usage (`DEF` and `USE`) to support slicing.

---

## 6. References
*   **Tree-sitter**: Used for robust, error-tolerant AST parsing.
*   **FalkorDB**: The destination graph database (Redis-based).
*   **Lombok**: Used for boilerplate reduction in Java entities.
