package com.jstarts.codegrapher.core.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassEntityTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSerializationRoundTrip() throws Exception {
        ClassEntity original = new ClassEntity.Builder()
                .id(UUID.randomUUID().toString())
                .name("MyClass")
                .type(CodeEntityType.CLASS)
                .location(new SourceLocation("src/app.py", 1, 10, 0, 5, 0, 100))
                .addModifier("public")
                .language("python")
                .build();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
        System.out.println(json);
        ClassEntity copy = mapper.readValue(json, ClassEntity.class);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getLocation().filePath(), copy.getLocation().filePath());
    }

}
