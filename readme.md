Of course. Here is a concise, actionable summary of the foundational plan for you, as Developer 1, to start coding immediately. This blueprint covers your setup, data models, and the first two major components you'll build.

---

### **Your Personal Coding Blueprint: The Foundation**

Your mission is to build the foundational layer of the Code Knowledge Graph. This involves parsing Java files to identify their structure (packages, files, types) and then writing that structure into the FalkorDB graph.

---

### **Step 1: Project Setup (`pom.xml`)**

Add the essential dependencies to your `pom.xml` to enable parsing, database connection, and data handling.

```xml
<dependencies>
    <!-- For parsing Java source code -->
    <dependency>
        <groupId>io.github.treesitter</groupId>
        <artifactId>java-tree-sitter</artifactId>
        <version>0.2.1</version>
    </dependency>

    <!-- For connecting to FalkorDB -->
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>5.1.3</version>
    </dependency>

    <!-- For easy DTO to JSON conversion for debugging -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

---

### **Step 2: Define Your Core Data Models (DTOs)**

Create these Java classes and enums. They are the "contract" that defines the structure of your parsed data.

1.  **`TypeKind.java` (Enum):** To differentiate between classes, interfaces, and enums.
    *   **File:** `.../dto/node/TypeKind.java`
    *   **Values:** `CLASS`, `INTERFACE`, `ENUM`.

2.  **`EdgeType.java` (Enum):** To represent all possible graph relationships in a type-safe way.
    *   **File:** `.../dto/edge/EdgeType.java`
    *   **Initial Values:** `CONTAINS`, `IMPORTS`. (Other developers will add more later).

3.  **`SourceLocation.java` (Class):** A shared utility to store location data.
    *   **File:** `.../dto/metadata/SourceLocation.java`
    *   **Fields:** `filePath`, `startLine`, `endLine`, etc.

4.  **`TypeDef.java` (Class):** Represents a single class, interface, or enum.
    *   **File:** `.../dto/node/TypeDef.java`
    *   **Fields:** `name`, `fullyQualifiedName`, `TypeKind type`, `SourceLocation location`.

5.  **`ParsedFile.java` (Class):** The main container that holds the result of parsing one file.
    *   **File:** `.../dto/ParsedFile.java`
    *   **Fields:** `filePath`, `PackageDef packageDef`, `List<ImportDef> imports`, `List<TypeDef> topLevelTypes`.

---

### **Step 3: Implement the Parser (`JavaParser.java`)**

This class is your "reader." Its only job is to read a `.java` file and translate it into your `ParsedFile` DTO.

*   **Input:** A `String` file path.
*   **Engine:** Use `Tree-sitter` and `TSQuery`.
*   **Logic:**
    1.  Find the `package_declaration` to create the `PackageDef`.
    2.  Find all `import_declaration` nodes to create a list of `ImportDef`s.
    3.  Find all top-level `class_declaration`, `interface_declaration`, and `enum_declaration` nodes.
    4.  For each declaration, create a `TypeDef` object, setting its `name`, `fullyQualifiedName`, and correct `TypeKind`.
*   **Output:** A fully populated `ParsedFile` DTO object.

---

### **Step 4: Implement the Ingestor (`GraphIngestor.java`)**

This class is your "writer." Its job is to take the DTO from the parser and write it to the database.

*   **Input:** A `ParsedFile` DTO object.
*   **Engine:** Use `Jedis` to connect to FalkorDB and execute Cypher queries.
*   **Logic for `File -> TypeDef`:**
    1.  **`MERGE` the File Node:** Create or find the `File` node.
        ```cypher
        MERGE (f:File {filePath: 'your/file/path.java'})
        ```
    2.  **Loop through `topLevelTypes`:** For each `TypeDef` in the DTO...
        a. **`MERGE` the Type Node:** Create or find the `Type` node using its unique FQN. Set its properties.
        ```cypher
        MERGE (t:Type {fqn: 'com.example.MyClass'})
        ON CREATE SET t.name = 'MyClass', t.kind = 'CLASS'
        ```
        b. **`MERGE` the CONTAINS Edge:** Create the relationship from the file to the type. Use the `EdgeType` enum to build the query string.
        ```cypher
        // In Java: "MERGE (f)-[:" + EdgeType.CONTAINS.name() + "]->(t)"
        MERGE (f)-[:CONTAINS]->(t)
        ```

---

### **Your Immediate Action Plan:**

1.  **Setup `pom.xml`.**
2.  **Create all the DTO files and Enums** listed in Step 2.
3.  **Build the `JavaParser` class** to populate these DTOs.
4.  **Use `Main.java`** to run the parser on a sample file and print the DTO as JSON to verify it works correctly.

Once the parser is working, you can then move on to building the `GraphIngestor`.
