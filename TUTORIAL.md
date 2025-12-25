# Code Grapher Tutorial

This tutorial guides you through setting up, building, running, and verifying the **Code Grapher** tool.

## 1. Prerequisites

Ensure you have the following installed:
-   **Java 21+**
-   **Maven**
-   **Docker** (for running FalkorDB)

## 2. Setup FalkorDB

Code Grapher uses **FalkorDB** (a Redis module) to store the code knowledge graph. The easiest way to run it is via Docker.

Run the following command to start a FalkorDB instance:

```bash
docker run -p 6379:6379 -it --rm falkordb/falkordb:edge
```

This starts FalkorDB on `localhost:6379`.

## 3. Build the Project

Navigate to the project root and build the application using Maven:

```bash
mvn clean package -DskipTests
```

This will create a runnable JAR file (or compile classes for `exec:java`).

## 4. Run Code Grapher

You can run the tool using the Maven `exec:java` goal.

### Basic Usage

To process a repository and store it in the default graph (`CodeGraph`):

```bash
mvn exec:java -Dexec.mainClass="com.jstarts.codegrapher.Main" -Dexec.args="--repo /path/to/target/repository"
```

### Advanced Usage

You can specify the database host, port, and a custom graph key:

```bash
mvn exec:java -Dexec.mainClass="com.jstarts.codegrapher.Main" -Dexec.args="--repo /path/to/target/repository --host localhost --port 6379 --graph MyCustomGraph"
```

**Arguments:**
-   `--repo <path>`: Absolute or relative path to the Python repository you want to analyze. (Required)
-   `--host <host>`: FalkorDB host (Default: `localhost`)
-   `--port <port>`: FalkorDB port (Default: `6379`)
-   `--graph <key>`: The key/name of the graph in FalkorDB (Default: `CodeGraph`)

## 5. Verify and Explore the Graph

Once the tool finishes, the data is stored in FalkorDB. You can explore it using `redis-cli` or any Redis GUI that supports FalkorDB.

### Using `redis-cli`

Connect to the database:
```bash
redis-cli
```

Run Cypher queries using the `falkordb.query` command.

#### Check Node Count
```redis
falkordb.query CodeGraph "MATCH (n) RETURN count(n)"
```

#### List All Files
```redis
falkordb.query CodeGraph "MATCH (f:File) RETURN f.name, f.filePath"
```

#### Find Function Calls
See which functions call which other functions:
```redis
falkordb.query CodeGraph "MATCH (c:Call)-[:CALLS]->(f:Function) RETURN c.callee, f.name"
```

#### Explore Variable Scopes
List variables and their scopes (GLOBAL, LOCAL, INSTANCE_FIELD):
```redis
falkordb.query CodeGraph "MATCH (v:Variable) RETURN v.name, v.scope, v.declaredType"
```

#### Find Class Hierarchy
```redis
falkordb.query CodeGraph "MATCH (c:Class) RETURN c.name, c.superClasses"
```

### Clearing a Graph

To delete an existing graph (e.g., to re-run extraction cleanly):

```bash
redis-cli falkordb.delete CodeGraph
```

## 6. Running Tests

To verify the system integrity, you can run the included integration tests:

```bash
mvn test -Dtest=VerifyGraph
```

This test connects to `localhost:6379`, checks for the existence of the graph, and validates some basic relationships from the sample project.
