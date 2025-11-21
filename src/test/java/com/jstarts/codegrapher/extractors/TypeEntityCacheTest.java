package com.jstarts.codegrapher.extractors;

import com.jstarts.codegrapher.core.entities.PythonType;
import com.jstarts.codegrapher.core.entities.TypeEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeEntityCacheTest {

    @Test
    void testGetOrCreateCanonicalization() {
        ExtractionContext context = new ExtractionContext();
        TypeEntityCache cache = context.getTypeEntityCache();

        PythonType type1 = PythonType.of("int");
        PythonType type2 = PythonType.of("int");

        TypeEntity entity1 = cache.getOrCreate(type1);
        TypeEntity entity2 = cache.getOrCreate(type2);

        assertSame(entity1, entity2, "TypeEntity objects should be the same instance");
        assertEquals(entity1.getId(), entity2.getId(), "TypeEntity IDs should be identical");

        // Verify it was added to context
        assertTrue(context.getAllExtractedEntities().contains(entity1));
        // Should be added only once? The cache adds it when created.
        // If we call getOrCreate again, it returns the same instance.
        // The context list will contain it once if we only created it once.
        // If we call getOrCreate multiple times, it doesn't add it again.
        assertEquals(1, context.getAllExtractedEntities().size());
    }

    @Test
    void testDifferentTypes() {
        ExtractionContext context = new ExtractionContext();
        TypeEntityCache cache = context.getTypeEntityCache();

        TypeEntity intEntity = cache.getOrCreate(PythonType.of("int"));
        TypeEntity strEntity = cache.getOrCreate(PythonType.of("str"));

        assertNotSame(intEntity, strEntity);
        assertNotEquals(intEntity.getId(), strEntity.getId());
        assertEquals(2, context.getAllExtractedEntities().size());
    }
}
