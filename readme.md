# Basic Overview of a Multi-Layer Code Knowledge Graph
Problem Definition: LLMs have a fixed context window length (Amount of tokens they can have inputted) thus, they are not able to properly perform repository level code tasks like code completion, issue fixing, and code generation. 

Mitigating hallucinations that are caused by the fixed context window length of LLMs.

Thus, there needs to be a way to efficiently give LLMs the proper code context regarding a specific code problem.

Proposed Solution: By using GraphRAG through code graph database, you can give the most contextually relevant code data and snippets for the LLM to solve these repository level code tasks.

- Mainly focusing on APR
Layer 1: Foundational Code Symbol/Entity Graph(Repository Context Graph) 10/2

- Sources 
- Semantic Code Graph (SCG) @borowskiSemanticCodeGraph2024
- @liuCodexGraphBridgingLarge2024
- @liuMarsCodeAgentAInative2024

- Purpose for Hallucination Mitigation: This layer provides the core structural map of the repository. It allows the LLM (or an agent) to:

- Quickly find relevant code entities based on names or types.
- Understand how entities are organized and related structurally.
- Verify if a generated class inherits correctly, or if a function call uses the right number/type of arguments based on signatures.
- Ground generated code to specific files and locations.
- This ensures that generated code patches or modifications can be applied precisely to the correct lines in the source files, preventing syntax errors or misapplications due to line number mismatches (a common issue with purely logical edits). (REPOGRAPH's strength).
Apply these graphs to existing approaches.
Layer 2: Behavioral/Program Analysis Integration 10/16
- Clarify Code Context Graph and Code Property Graph
- Building a local Code Context Graph (CCG)
- Sources 
- @liuGraphCoderEnhancingRepositoryLevel2024
- @abdelazizToolkitGeneratingCode2021
- Code Property Graph (CPG)
- Ensures generated code follows potential execution paths and correctly handles data flow.
- Helps verify that a generated patch or new function integrates correctly with the internal logic of existing functions (using CCG slice analysis).
- Prevents misuse of variables or objects by understanding their dependencies (data flow).
