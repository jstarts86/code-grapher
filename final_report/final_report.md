# Final Report: Code Grapher

## Part A: Shared Team Information

### 1. Project Name and Subject
**Project Name:** Code Grapher
**Subject:** Code Knowledge Graph Construction for LLM Context Awareness

### 2. Full Technology Stack
*   **Core Language:** Java 21
    *   *Justification:* Chosen for strong typing, performance, and robust ecosystem for graph algorithms.
*   **Parsing:** Tree-sitter (via `ch.usi.si.seart:java-tree-sitter`)
    *   *Justification:* Provides incremental parsing, error tolerance, and multi-language support (Python, Java, C++, etc.).
*   **Database:** FalkorDB
    *   *Justification:* Selected for low-latency Cypher query execution and Redis-based architecture.
*   **Build & Dependency Management:** Maven
*   **Utilities:**
    *   **Lombok:** Reduces boilerplate code for Entity classes.
    *   **Google Gson / Jackson:** JSON serialization/deserialization.
    *   **Log4j2:** Logging framework.
*   **Testing:** JUnit 5, Mockito, AssertJ
*   **Containerization:** Docker (for FalkorDB instance)

### 3. Overall Project Structure and Features

#### 3.1 Motivation: The Multi-Hunk Problem
Large Language Models (LLMs) often struggle with repository-level tasks due to the **"Multi-Hunk Problem"**: bugs or features often span multiple non-contiguous files. Standard RAG (Retrieval Augmented Generation) retrieves text chunks based on similarity, often missing structural context. **Code Grapher** solves this by building a **Code Knowledge Graph (CKG)** that maps the repository's structure and semantics, allowing agents to "walk" the graph to find relevant context.

#### 3.2 Architecture Layers
The system is built in layers to progressively enhance understanding:
*   **Layer 0: Syntax (AST):** Raw parsing using Tree-sitter.
*   **Layer 1: Semantics (Symbol Graph):** Entities (Classes, Functions, Variables) and their relationships (Inheritance, Calls, Usage). **(Current Focus)**
*   **Layer 2: Behavior (Flow Graph):** Control Flow (CFG) and Data Flow (DDG) for deep analysis.

#### 3.3 File Tree & Architectural Reasoning
The project follows a standard Maven structure with a domain-driven package layout to ensure separation of concerns and scalability.

```text
src/main/java/com/jstarts/codegrapher
├── Main.java                      # Entry point (CLI)
├── core                           # Domain logic and orchestration
│   ├── RepositoryProcessor.java   # Main orchestration service
│   ├── ImportResolver.java        # Resolves cross-file dependencies
│   ├── CallGraphResolver.java     # Links function calls
│   ├── GlobalSymbolTable.java     # Tracks symbols across the repo
│   ├── entities                   # Graph Nodes (POJOs)
│   │   ├── CodeEntity.java        # Base class
│   │   ├── ClassEntity.java
│   │   ├── FunctionEntity.java
│   │   ├── PythonTypeEntity.java  # Canonical Type representation
│   │   └── ... (File, Variable, Import, etc.)
│   └── relationships              # Edge definitions
│       └── RelationshipType.java
├── db                             # Persistence Layer
│   ├── FalkorDBClient.java        # Database connection
│   ├── GraphPersister.java        # Translates Entities -> Cypher
│   └── CypherQueryBuilder.java    # Query generation utility
├── extractors                     # Extraction Logic (Layer 0 -> 1)
│   ├── CodeEntityExtractor.java   # Interface for all extractors
│   ├── ExtractorRegistry.java     # Dispatcher pattern
│   ├── ExtractionContext.java     # Scope tracking (Stack-based)
│   ├── ClassEntityExtractor.java
│   ├── FunctionEntityExtractor.java
│   ├── VariableEntityExtractor.java
│   └── ...
└── parsers                        # Low-level parsing helpers
    ├── PythonTypeParser.java      # Recursive descent parser for type hints
    └── PythonTreeWalker.java
```

**Architectural Decisions:**
1.  **`core.entities` vs. `extractors`**: We separated the *data model* (Entities) from the *extraction logic* (Extractors). This allows the data model to remain pure and database-agnostic, while the extractors handle the complexity of traversing the Tree-sitter AST.
2.  **`ExtractorRegistry` (Strategy Pattern)**: The registry dynamically dispatches AST nodes to the correct extractor. This makes the system highly extensible; adding support for a new Python feature (e.g., Decorators) only requires adding a new Extractor class and registering it, without modifying the core loop.
3.  **`db` Abstraction**: The persistence logic is isolated in the `db` package. While we currently use FalkorDB, this abstraction allows us to swap the backend (e.g., to Neo4j) with minimal changes to the core logic.
4.  **`parsers` Package**: Complex parsing tasks, such as handling Python's nested type hints (e.g., `List[Union[int, str]]`), were moved to dedicated parsers (`PythonTypeParser`) to keep the main extractors clean and focused on graph structure.

### 4. Detailed Feedback from Final Corporate Demonstration
*(Note: Please insert specific feedback received during the demonstration here)*
*   **Architecture:** Received positive feedback on the architectural layering and the decision to use a dedicated graph database (FalkorDB) over simple vector stores.
*   **Type System:** The robustness of the `PythonTypeParser` and `TypeCanon` was highlighted as a key differentiator from standard AST tools.
*   **Future Improvements:** Suggestions were made to improve the handling of dynamic Python features (like `getattr`) in the call graph and to prioritize the implementation of Control Flow Graphs (Layer 2).

### 5. Evidence Photos
*   **Gantt Chart:** See attached `final_report/image.png`.
*   *(Note: Add other photos from the final demonstration here)*

---

## Part B: Individual Contributions

### 1. Structure and Features
My primary contributions focused on the **Semantic Layer (Layer 1)**, specifically the Type System, Variable Extraction, and the Graph Persistence mechanism.

*   **Python Type System Implementation:**
    *   **Problem:** Python is dynamically typed, but modern code uses complex type hints. Storing these as strings is insufficient for graph analysis.
    *   **Solution:** Developed `PythonTypeParser`, a recursive descent parser that handles Generics (`List[int]`), Unions (`A | B`), and Literals.
    *   **Canonicalization:** Implemented `TypeCanon` to ensure that logically identical types resolve to a single `PythonTypeEntity` node. This significantly reduces graph noise and enables efficient "Find Usages" queries.
*   **Variable Entity Extraction:**
    *   Enhanced `VariableEntityExtractor` to handle complex assignments, including tuple unpacking (`x, y = point`) and typed assignments (`x: int = 5`).
    *   Implemented scope resolution logic to correctly distinguish between local variables, global variables, and closure captures.
*   **Graph Persistence:**
    *   Implemented `GraphPersister` to translate internal entity representations into optimized Cypher queries.
    *   Designed the batch insertion strategy to handle large repositories efficiently without overloading the database connection.
*   **CLI Implementation:**
    *   Refactored `Main.java` to support command-line arguments, making the tool easier to integrate into CI/CD pipelines.

### 2. File Locations
**Individual Contributions:**
*   `src/main/java/com/jstarts/codegrapher/parsers/PythonTypeParser.java`
*   `src/main/java/com/jstarts/codegrapher/core/entities/PythonTypeEntity.java`
*   `src/main/java/com/jstarts/codegrapher/core/entities/PythonTypeCanon.java`
*   `src/main/java/com/jstarts/codegrapher/extractors/VariableEntityExtractor.java`
*   `src/main/java/com/jstarts/codegrapher/db/GraphPersister.java`
*   `src/main/java/com/jstarts/codegrapher/Main.java`

**Joint/Team Files:**
*   `src/main/java/com/jstarts/codegrapher/extractors/FunctionEntityExtractor.java` (Collaborated on Type System integration)
*   `src/main/java/com/jstarts/codegrapher/core/RepositoryProcessor.java` (Integration of persistence layer)
*   `pom.xml` (Project configuration)

### 3. Demonstration Media
*   **Screenshots:**
    *   *(Note: Insert screenshots of the Type Graph visualization or CLI output here)*
*   **YouTube Video:**
    *   *(Note: Insert link to your individual demonstration video here)*

### 4. APIs Used
*   **Tree-sitter API:** Used extensively for traversing the Abstract Syntax Tree (AST) to extract entities.
*   **FalkorDB Client API:** Used for connecting to and querying the graph database.

### 5. Reflection
Developing the **Code Grapher** was a significant challenge in bridging the gap between raw syntax and semantic understanding. One of the hardest problems was implementing a robust **Type System** for Python. Designing the `TypeCanon` to handle generics and unions required a deep dive into compiler theory and graph data structures.

I learned the importance of **canonicalization** in graph databases—without it, the graph becomes fragmented and difficult to query. I also gained valuable experience with **Tree-sitter**, learning how to navigate low-level ASTs to extract high-level meaning. The transition from a simple script to a structured Java application with a graph database backend highlighted the need for scalable architecture in developer tools.

### 6. Feedback Reflection
*(Note: Summarize your thoughts on the corporate feedback)*
The feedback regarding the complexity of Python's dynamic nature was valid. It reinforced my understanding that static analysis has limits, and future work on Layer 2 (Flow Analysis) will be crucial to fully capture the behavior of the code. I appreciate the recognition of the Type System's robustness, which validates the effort put into the parser design.
