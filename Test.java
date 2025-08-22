package com.jstarts.test;

import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * A custom annotation to test parsing.
 */
@interface MyAnnotation {
    String value();
    int count() default 1;
}

/**
 * An interface that the main test class will implement.
 */
interface MyInterface {
    void performAction(String action);
    default void log() {
        System.out.println("Default log.");
    }
}

/**
 * A base class for the main test class to extend.
 */
class SomeBaseClass {
    protected String baseField = "base";
}

/**
 * This is the main test class, designed to be rich in Java grammar
 * to thoroughly test the code grapher's parsing capabilities.
 */
@MyAnnotation("class-level")
public class Test extends SomeBaseClass implements MyInterface {

    // --- Fields (Node: Field, Edge: CONTAINS) ---
    @MyAnnotation("field-level")
    private int instanceField = 0;
    public static final String CONSTANT_STRING = "Hello, World!";
    private final List<String> dataList;

    // --- Nested Types (Node: Type, Edge: CONTAINS) ---
    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED
    }

    static class NestedStaticClass {
        private int nestedField;
    }

    // --- Constructor ---
    public Test(List<String> initialData) {
        this.dataList = initialData;
    }

    // --- Methods (Node: Method, Edge: CONTAINS) ---

    /**
     * A method demonstrating parameters, return types, local variables,
     * field access, and method calls.
     * @param items A map of items to process.
     * @return A list of processed keys.
     */
    @MyAnnotation("method-level")
    public List<String> processData(Map<String, Integer> items, @MyAnnotation("param-level") int threshold) {
        // Local Variable (Node: LocalVariable)
        int processedCount = 0;

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            if (entry.getValue() > threshold) {
                // Method Accesses Field (Edge: ACCESSES - write)
                this.instanceField++;
                processedCount++;
            }
        }

        // Method Calls Method (Edge: CALLS)
        utilityMethod(processedCount);

        // Method Accesses Field (Edge: ACCESSES - read)
        System.out.println("Total processed: " + this.instanceField);

        // Method Returns Type (Edge: RETURNS)
        return items.keySet().stream().collect(toList());
    }

    @Override
    public void performAction(String action) {
        // Anonymous inner class
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Action: " + action);
            }
        };
        new Thread(r).start();
    }

    private static void utilityMethod(int count) {
        System.out.println("Utility method called with count: " + count);
    }
}