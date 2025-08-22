### **To: Gemini Code Agent**
### **Subject: High-Level Plan to Refactor `codegrapher` into a Two-Phase Graph-Building Parser**

#### **1. Objective**

The goal is to evolve the current `JavaParser` into a highly efficient and extensible architecture that builds a complete code graph for an entire project. We will implement a **two-phase process**:

1.  **Phase 1 (Intra-file):** A fast, single-pass parse of each file to build a "local" graph of its internal nodes and relationships.
2.  **Phase 2 (Inter-file):** A project-wide analysis that "stitches" these local graphs together by resolving relationships that cross file boundaries (e.g., method calls, inheritance).

This approach maximizes performance by parallelizing the parsing of individual files and then handling the complex cross-file logic in a dedicated second step.

#### **2. Current State Analysis**

The existing `JavaParser` is inefficient because it re-parses or re-walks the AST for each type of entity it needs to find. This approach is slow and becomes difficult to maintain or extend as we want to extract more information from the source code.

#### **3. Target Architecture: The Visitor/Dispatcher Model**

Our new architecture will consist of four key concepts for Phase 1:

1.  **The Contract (`CodeEntityExtractor` interface):** A simple interface that defines what it means to be an "extractor."
2.  **The Engine (`TreeWalker`):** This class will perform a single, depth-first traversal of the AST for one file. **It will maintain a context stack to know its current location within the code structure (e.g., inside a class, inside a method).**
3.  **The Collector (`GraphBuilder`):** A stateful object that accumulates all nodes and *intra-file* edges discovered during a single file walk.
4.  **The Workers (`Extractor` implementations):** Focused classes responsible for handling specific AST node types and updating the shared `GraphBuilder`.

---

### **4. Step-by-Step Implementation Plan**

### **Phase 1: Building the Intra-File Graph**

#### **Step 1: Lay the Foundation - The Core Components**

First, create the core classes that enable the single-file parsing architecture.

*   **`CodeEntityExtractor` Interface:**
    *   **[MODIFICATION] Add Context to the Contract:** Define this with a method that accepts the current context: `extract(Node node, String sourceCode, GraphBuilder graphBuilder, NodeDef currentContext)`. The `currentContext` will be the parent entity (e.g., `ClassDef`, `MethodDef`).

*   **`TreeWalker` Class:**
    *   **[MODIFICATION] Implement Context-Aware Traversal:**
        *   Add a `Deque<NodeDef> contextStack` to the `TreeWalker`. A `Deque` is used as a stack.
        *   The `walk(Node node)` method will be recursive and manage the stack:
            1.  Get the current context for this node: `NodeDef context = contextStack.peek()`.
            2.  Invoke the registered extractors for the current `node`, passing them the `context`.
            3.  **Crucially, check if the current `node` defines a new scope** (e.g., `class_declaration`, `method_declaration`).
            4.  If it does, get the `NodeDef` that was just created by the extractor (via the `GraphBuilder`) and `contextStack.push()` it.
            5.  Recursively call `walk()` on all child nodes.
            6.  After the recursion returns, if a context was pushed in step 4, `contextStack.pop()` it to restore the previous scope. This is vital.

*   **`GraphBuilder` Class:**
    *   Create this collector to manage an internal map of nodes (`Map<FQN, NodeDef>`) and a list of edges (`List<Edge>`) for a single file.
    *   **[MODIFICATION] Add a Helper for Context:** Add a method like `getLastAddedNode()` so the `TreeWalker` can retrieve the `NodeDef` that was just created by an extractor and push it onto its context stack.

#### **Step 2: Create Extractors and Establish *Local* Relationships**

Start by migrating logic for a primary entity, like a class, into its own extractor.

1.  **Create `extractors` Package:** Organize your new components in `...codegrapher.graph.parser.extractors`.
2.  **Implement `ClassExtractor`:** Inside its `extract` method, parse the `class_declaration` node to create a `ClassDef` DTO.
3.  **Build Intra-File Edges:**
    *   **Register the Node:** Call `graphBuilder.registerNode(classDef)`.
    *   **[MODIFICATION] Create Contextual `CONTAINS` Edge:** Use the `currentContext` passed into the `extract` method. Assert that it's a `FileDef` (or `ClassDef` for inner classes) and create the edge: `graphBuilder.addEdge(currentContext, classDef, "CONTAINS")`. This correctly establishes the parent-child relationship.
    *   **Handle Unresolved References:** For `EXTENDS` or `IMPLEMENTS`, create the edge using a placeholder DTO for the target, as originally planned.

#### **Step 3: Refactor `JavaParser` into a Phase 1 Coordinator**

The `JavaParser` class will now be responsible for executing the Phase 1 analysis for a single file.

1.  **Instantiate Components:** In its `parse` method, create instances of the `GraphBuilder` and `TreeWalker`.
2.  **[MODIFICATION] Create Initial Context:**
    *   Create a `FileDef` node representing the file being parsed.
    *   Register this `FileDef` with the `graphBuilder`.
    *   This `FileDef` is the root of your intra-file graph and the initial context for the walk.
3.  **Register Workers:** Register all your extractor implementations with the `TreeWalker`.
4.  **Initiate the Walk:** A single call to `walker.walk(rootNode, fileDef)` will execute the entire intra-file analysis, starting with the file as the top-level context.
5.  **Return the Local Graph:** The `parse` method will now return a `ParsedFile` DTO, which contains the collection of nodes and edges discovered in that file.

#### **Step 4: Expand with More Extractors**

Incrementally repeat Step 2 for all other entities, leveraging the context.

*   **`MethodExtractor`:** Its `extract` method will receive a `ClassDef` as its `currentContext`, allowing it to correctly create a `CONTAINS` edge from the class to the method.
*   **`FieldExtractor`:** Similarly, it will receive a `ClassDef` as its context.
*   **`LocalVariableExtractor`:** This is where context is powerful. This extractor would be registered for `local_variable_declaration` nodes. Its `currentContext` would be a `MethodDef`, correctly scoping the variable and distinguishing it from a `FieldDef`.

For relationships that might be external (`CALLS`, `ACCESSES`), you still record the target's FQN and mark it for resolution in Phase 2.

---

### **Phase 2: Stitching the Project Graph Together**

This phase remains conceptually the same, but its input is now more accurate.

#### **Step 5: Create a Project-Level Analyzer**

Introduce a new class, `ProjectAnalyzer`, to orchestrate this phase.

1.  **Parsing All Files:** It will iterate through every `.java` file, calling `JavaParser.parse()` on each one to get a list of `ParsedFile` objects.
2.  **Building a Global Index:** As each `ParsedFile` is generated, the `ProjectAnalyzer` will populate a global map (`Map<FQN, NodeDef>`).
    *   **[MODIFICATION] Benefit of Context:** The `CONTAINS` relationships within each `ParsedFile` are now guaranteed to be correct (File -> Class -> Method), providing a richer, more accurate local graph before stitching even begins.

#### **Step 6: Resolve Inter-File Relationships**

Once all files have been parsed and the global index is complete, the final "stitching" process can begin.

1.  **Iterate and Resolve:** The `ProjectAnalyzer` will loop through each `ParsedFile` object again.
2.  **Connect the Dots:** For each edge with an unresolved placeholder, it will look up the target FQN in the global index and replace the placeholder with the actual `NodeDef` object.
3.  **Produce the Final Graph:** After resolving all cross-file references, the `ProjectAnalyzer` will hold the complete, interconnected graph of your entire codebase.
