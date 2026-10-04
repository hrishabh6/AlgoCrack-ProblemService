package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class UndirectedGraphEdgesGenerator implements BenchmarkGenerator {

    private final ObjectMapper objectMapper;

    public UndirectedGraphEdgesGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String key() {
        return "UNDIRECTED_GRAPH_EDGES";
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public Set<String> supportedVariants() {
        return Set.of("SPARSE", "CHAIN", "RANDOM");
    }

    @Override
    public ArrayNode generateInput(Map<String, Integer> sizeVector, String variant, long seedLong) {
        int v = sizeVector.getOrDefault("v", 0);
        int e = sizeVector.getOrDefault("e", 0);
        if (v < 2) {
            throw new IllegalArgumentException("v must be at least 2");
        }
        int maxEdges = v * (v - 1) / 2;
        if (e < v - 1 || e > maxEdges) {
            throw new IllegalArgumentException("invalid edge count for v");
        }
        Random random = new Random(seedLong);
        ArrayNode edges = objectMapper.createArrayNode();
        Set<String> seen = new HashSet<>();
        if ("CHAIN".equals(variant)) {
            for (int i = 0; i < v - 1; i++) {
                edges.add(edgeNode(objectMapper, i, i + 1));
            }
            int remaining = e - (v - 1);
            for (int i = 0; i < remaining; i++) {
                addRandomEdge(random, v, edges, seen, objectMapper);
            }
        } else {
            for (int i = 0; i < v - 1; i++) {
                edges.add(edgeNode(objectMapper, i, i + 1));
                seen.add(canonicalEdge(i, i + 1));
            }
            while (seen.size() < e) {
                addRandomEdge(random, v, edges, seen, objectMapper);
            }
        }
        ArrayNode args = objectMapper.createArrayNode();
        args.add(v);
        args.add(edges);
        return args;
    }

    private static void addRandomEdge(Random random, int v, ArrayNode edges, Set<String> seen, ObjectMapper mapper) {
        for (int attempt = 0; attempt < 100; attempt++) {
            int a = random.nextInt(v);
            int b = random.nextInt(v);
            if (a == b) {
                continue;
            }
            int lo = Math.min(a, b);
            int hi = Math.max(a, b);
            String key = canonicalEdge(lo, hi);
            if (seen.add(key)) {
                edges.add(edgeNode(mapper, lo, hi));
                return;
            }
        }
        throw new IllegalStateException("could not sample unique edge");
    }

    private static ArrayNode edgeNode(ObjectMapper mapper, int a, int b) {
        ArrayNode edge = mapper.createArrayNode();
        edge.add(a);
        edge.add(b);
        return edge;
    }

    private static String canonicalEdge(int a, int b) {
        return a + ":" + b;
    }
}
