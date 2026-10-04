package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hrishabh.problemservice.helper.Validation;
import com.hrishabh.problemservice.models.QuestionMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeededGeneratorSemanticsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validation validation = new Validation();

    @Test
    void fourSumProfileProducesQuadrupleSummingToTarget() throws Exception {
        IntArrayWithTargetGenerator generator = new IntArrayWithTargetGenerator(objectMapper);
        ArrayNode input = generator.generateInput(Map.of("n", 64), "RANDOM", 12345L);
        assertTrue(hasFourSum(input.get(0), input.get(1).asInt()));
        validation.validateTestCaseInput(objectMapper.writeValueAsString(input), fourSumMetadata());
    }

    @Test
    void trapRainMatrixRespectsDimensionsAndHeightBounds() throws Exception {
        IntMatrixRowsColsGenerator generator = new IntMatrixRowsColsGenerator(objectMapper);
        ArrayNode input = generator.generateInput(Map.of("n", 8, "m", 16), "RANDOM", 7L);
        JsonNode matrix = input.get(0);
        assertEquals(8, matrix.size());
        assertEquals(16, matrix.get(0).size());
        for (JsonNode row : matrix) {
            for (JsonNode cell : row) {
                int height = cell.asInt();
                assertTrue(height >= 0 && height <= 100_000, "height out of problem bounds");
            }
        }
        validation.validateTestCaseInput(objectMapper.writeValueAsString(input), matrixMetadata());
    }

    @Test
    void criticalConnectionsGraphIsConnectedWithValidEdgeCount() throws Exception {
        UndirectedGraphEdgesGenerator generator = new UndirectedGraphEdgesGenerator(objectMapper);
        ArrayNode input = generator.generateInput(Map.of("v", 32, "e", 48), "CHAIN", 99L);
        int v = input.get(0).asInt();
        JsonNode edges = input.get(1);
        assertEquals(48, edges.size());
        assertTrue(isConnected(v, edges));
        assertEquals(48, uniqueUndirectedEdges(edges));
        validation.validateTestCaseInput(objectMapper.writeValueAsString(input), graphMetadata());
    }

    @Test
    void chainVariantDoesNotDuplicateEdges() {
        UndirectedGraphEdgesGenerator generator = new UndirectedGraphEdgesGenerator(objectMapper);
        ArrayNode input = generator.generateInput(Map.of("v", 16, "e", 20), "CHAIN", 1L);
        assertEquals(20, uniqueUndirectedEdges(input.get(1)));
    }

    private static QuestionMetadata fourSumMetadata() {
        return QuestionMetadata.builder()
                .paramTypes(List.of("int[]", "int"))
                .build();
    }

    private static QuestionMetadata matrixMetadata() {
        return QuestionMetadata.builder()
                .paramTypes(List.of("int[][]"))
                .build();
    }

    private static QuestionMetadata graphMetadata() {
        return QuestionMetadata.builder()
                .paramTypes(List.of("int", "int[][]"))
                .build();
    }

    private static boolean hasFourSum(JsonNode numsNode, int target) {
        int n = numsNode.size();
        for (int a = 0; a < n - 3; a++) {
            for (int b = a + 1; b < n - 2; b++) {
                for (int c = b + 1; c < n - 1; c++) {
                    for (int d = c + 1; d < n; d++) {
                        int sum = numsNode.get(a).asInt() + numsNode.get(b).asInt()
                                + numsNode.get(c).asInt() + numsNode.get(d).asInt();
                        if (sum == target) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static int uniqueUndirectedEdges(JsonNode edges) {
        Set<String> seen = new HashSet<>();
        for (JsonNode edge : edges) {
            int a = edge.get(0).asInt();
            int b = edge.get(1).asInt();
            int lo = Math.min(a, b);
            int hi = Math.max(a, b);
            assertNotEquals(lo, hi);
            seen.add(lo + ":" + hi);
        }
        return seen.size();
    }

    private static boolean isConnected(int vertices, JsonNode edges) {
        List<Set<Integer>> adj = new java.util.ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adj.add(new HashSet<>());
        }
        for (JsonNode edge : edges) {
            int a = edge.get(0).asInt();
            int b = edge.get(1).asInt();
            adj.get(a).add(b);
            adj.get(b).add(a);
        }
        Set<Integer> visited = new HashSet<>();
        dfs(0, adj, visited);
        return visited.size() == vertices;
    }

    private static void dfs(int node, List<Set<Integer>> adj, Set<Integer> visited) {
        if (!visited.add(node)) {
            return;
        }
        for (int next : adj.get(node)) {
            dfs(next, adj, visited);
        }
    }
}
