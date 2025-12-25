package com.jstarts.codegrapher.db;

import com.jstarts.codegrapher.core.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GraphPersisterTest {

    @Mock
    private FalkorDBClient client;

    @InjectMocks
    private GraphPersister persister;

    @Test
    void testPersistFileEntity() {
        FileEntity file = new FileEntity.Builder()
                .id("file1")
                .name("main.py")
                .location(new SourceLocation("main.py", 1, 10, 1, 1, 0, 100))
                .build();

        persister.persist(List.of(file));

        // 1 call for node creation, 0 for relationships (no parent)
        verify(client, times(1)).executeQuery(anyString(), any(Map.class));
    }

    @Test
    void testPersistRelationships() {
        FileEntity parent = new FileEntity.Builder()
                .id("file1")
                .name("main.py")
                .build();

        ClassEntity child = new ClassEntity.Builder()
                .id("class1")
                .name("MyClass")
                .parentId("file1")
                .build();

        persister.persist(List.of(parent, child));

        // 2 calls for nodes
        // 1 call for PARENT_OF relationship
        verify(client, times(3)).executeQuery(anyString(), any(Map.class));
    }

    @Test
    void testPersistCall() {
        CallEntity call = new CallEntity.Builder()
                .id("call1")
                .name("my_func")
                .callee("my_func")
                .argCount(0)
                .parentId("file1")
                .build();
        call.setResolvedFunctionId("func1");

        persister.persist(List.of(call));

        // 1 node
        // 1 relationship (PARENT_OF)
        // 1 relationship (CALLS)
        verify(client, times(3)).executeQuery(anyString(), any(Map.class));
    }
}
