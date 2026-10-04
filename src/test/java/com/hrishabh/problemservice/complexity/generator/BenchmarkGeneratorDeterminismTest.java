package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkGeneratorDeterminismTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void intArrayWithTargetIsDeterministic() {
        IntArrayWithTargetGenerator generator = new IntArrayWithTargetGenerator(objectMapper);
        ArrayNode first = generator.generateInput(Map.of("n", 32), "RANDOM", 42L);
        ArrayNode second = generator.generateInput(Map.of("n", 32), "RANDOM", 42L);
        assertEquals(first.toString(), second.toString());
    }

    @Test
    void matrixPreservesIndependentDimensions() {
        IntMatrixRowsColsGenerator generator = new IntMatrixRowsColsGenerator(objectMapper);
        ArrayNode input = generator.generateInput(Map.of("n", 5, "m", 7), "RANDOM", 99L);
        assertEquals(5, input.get(0).size());
        assertEquals(7, input.get(0).get(0).size());
    }

    @Test
    void graphGeneratorRejectsImpossibleEdgeCount() {
        UndirectedGraphEdgesGenerator generator = new UndirectedGraphEdgesGenerator(objectMapper);
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateInput(Map.of("v", 4, "e", 100), "SPARSE", 1L));
    }

    @Test
    void unsupportedVariantRejectedByRegistry() {
        BenchmarkGeneratorRegistry registry = new BenchmarkGeneratorRegistry(java.util.List.of(
                new IntArrayWithTargetGenerator(objectMapper)));
        assertTrue(registry.find("INT_ARRAY_WITH_TARGET", "v1").isPresent());
        assertTrue(registry.find("UNKNOWN", "v1").isEmpty());
    }
}
