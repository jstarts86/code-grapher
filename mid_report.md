# Code Grapher: Mid-Term Progress Report
**Date:** December 1, 2025  
**Status:** Active Development  

---

## 1. Executive Summary
The **Code Grapher** project aims to build a sophisticated context retrieval tool designed to solve the **"Multi Hunk Problem"** and mitigate hallucinations in Large Language Models (LLMs), specifically for Automated Program Repair (APR) tasks. By constructing a rich **Code Knowledge Graph (CKG)**, the system provides LLMs with structurally and semantically accurate context, enabling them to understand repository-level relationships that simple text retrieval (RAG) misses.

## 2. Problem & Solution

### The Challenge
*   **Fixed Context Windows**: LLMs cannot ingest entire repositories.
*   **The Multi Hunk Problem**: Bugs often span multiple files or functions. Retrieving only the immediate function is insufficient.
*   **Hallucinations**: Without a structural map, LLMs invent methods or misinterpret class hierarchies.

### The Solution: GraphRAG
We propose a multi-layered graph approach:
*   **Layer 1: Foundational Code Symbol/Entity Graph** (Repository Map)
    *   *Purpose*: Quick lookups, inheritance tracking, signature verification.
    *   *Status*: **Mostly Complete**.
*   **Layer 2: Behavioral/Program Analysis Integration** (Execution Logic)
    *   *Purpose*: Control flow (CFG), Data flow (DDG), and Slicing.
    *   *Status*: **Planning / Early Prototyping**.

---

## 3. Implementation Status (Layer 1)

We have successfully implemented the core extraction engine using **Java** and **Tree-sitter**. The system parses Python code into a structured graph format compatible with **FalkorDB**.

### 3.1 Core Extractors
| Component | Status | Description |
| :--- | :--- | :--- |
| **File/Module** | ✅ Done | Extracts file hierarchy and module structure. |
| **Classes** | ✅ Done | Captures class definitions and inheritance relationships. |
| **Functions** | ✅ Done | detailed extraction of signatures, async status, and decorators. |
| **Fields** | ✅ Done | Extracts class attributes and member variables. |
| **Imports** | ✅ Done | Resolves internal and external dependencies. |
| **Variables** | 🚧 In Progress | Local variable extraction and scoping. |

### 3.2 Feature Spotlight: Type System Implementation (New)
*Recent work (Nov 27 - Dec 1) focused on a robust Type Extraction system.*

*   **`PythonTypeParser`**: A new recursive parser that handles complex Python type hints.
    *   **Generics**: Correctly parses nested types like `List[Dict[str, int]]`.
    *   **Unions**: Normalizes `Union[A, B]` and `A | B`.
    *   **Normalization**: Built-in types are canonicalized (e.g., `str`, `int`).
*   **`TypeCanon`**: Implemented a canonicalization registry to ensure that identical types share the same graph node (deduplication).
*   **Integration**: The `FunctionEntityExtractor` was updated to resolve:
    *   Parameter Types (e.g., `def foo(x: int)`)
    *   Return Types (e.g., `-> str`)
    *   These are now linked to `PythonTypeEntity` objects rather than just raw strings.

### 3.3 Infrastructure
*   **Graph Database**: **FalkorDB** integration is active for storing and querying the graph.
*   **Parsing Engine**: **Tree-sitter** is used for robust, error-tolerant parsing of source code.

---

## 4. Technical Architecture

### Data Model (Entities)
The graph is built from the following core entities:
*   `FileEntity`: Represents a source file.
*   `ClassEntity`: Represents a class definition.
*   `FunctionEntity`: Represents a function or method.
    *   *Attributes*: `name`, `parameters`, `returnType`, `isAsync`.
*   `PythonTypeEntity`: **[NEW]** Represents a distinct type (e.g., `List[int]`).
*   `VariableEntity`: Represents a variable declaration.

### The Code Property Graph (CPG) Vision
We are moving towards a full CPG that unifies:
1.  **AST** (Abstract Syntax Tree) - *Implemented via Tree-sitter*
2.  **CFG** (Control Flow Graph) - *Next Phase*
3.  **PDG** (Program Dependence Graph) - *Future Phase*

---

## 5. Roadmap & Next Steps

### Immediate Priorities (Dec 1 - Dec 15)
1.  **Complete Variable Extraction**: Finalize local variable scoping and linking to types.
2.  **Call Graph Construction**: Implement a `CallExtractor` to link function calls (`CALLS` edges) to their definitions. This is critical for traversing the graph.
3.  **Type Propagation**: Use the new `PythonTypeEntity` nodes to propagate type information through variables and function calls.

### Future Goals (Layer 2)
*   **Control Flow Analysis**: Build CFGs for functions to detect unreachable code and valid execution paths.
*   **Slicing Support**: Implement backward/forward slicing to retrieve only relevant code chunks for the LLM (solving the Context Window limit).

---

## 6. Appendix: References & Resources
*   **Papers**:
    *   *Semantic Code Graph (SCG)* (Borowski, 2024)
    *   *GraphCoder* (Liu, 2024)
*   **Tools**:
    *   [FalkorDB](https://www.falkordb.com/)
    *   [Tree-sitter](https://tree-sitter.github.io/)
    *   [LangChain4j](https://github.com/langchain4j/langchain4j)