package com.hrishabh.problemservice.complexity.support;

import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileValidator.MeasurementLimits;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileSeededHashTest {

    @Test
    void canonicalHashIsStableForRepeatedComputation() {
        String first = trapRainHash();
        String second = trapRainHash();
        assertEquals(first, second);
    }

    @Test
    void seededV5ProfileHashes() {
        assertEquals("9318de79c2efbdb1b00e72b4b54a13c42e63bc482c3136fdfd60ee12a06dadb1", trapRainHash());
        assertEquals("72f2b1a01bbd0b0b00c85d32ec06088dd2ed045a430452b1b687623973660075", criticalConnectionsHash());
        assertEquals("18a8ce82859f2c884b71c98eccc46c43c24467c68c92659c369482120e5efd10", fourSumHash());
    }

    static String trapRainHash() {
        return ProfileCanonicalJson.profileHash(
                1L, "JAVA", "TRAP_RAIN_MATRIX", "v1", "INT_MATRIX_ROWS_COLS", "v1",
                List.of(
                        new VariableDefinition("n", "rows of heightMap", "heightMap", "rows"),
                        new VariableDefinition("m", "columns of heightMap", "heightMap", "cols")),
                Map.of("n", List.of(8, 16, 32), "m", List.of(8, 16, 32)),
                Map.of("n", 64, "m", 64),
                List.of("RANDOM", "FLAT", "PEAK"),
                new MeasurementLimits(3, 5, 2000, 20000));
    }

    static String criticalConnectionsHash() {
        return ProfileCanonicalJson.profileHash(
                3L, "JAVA", "CRITICAL_CONNECTIONS_GRAPH", "v1", "UNDIRECTED_GRAPH_EDGES", "v1",
                List.of(
                        new VariableDefinition("v", "number of nodes", "n", null),
                        new VariableDefinition("e", "number of edges", "connections", "edgeCount")),
                Map.of("v", List.of(32, 64), "e", List.of(48, 96)),
                Map.of("v", 128, "e", 256),
                List.of("SPARSE", "CHAIN", "RANDOM"),
                new MeasurementLimits(3, 5, 2000, 20000));
    }

    static String fourSumHash() {
        return ProfileCanonicalJson.profileHash(
                10L, "JAVA", "FOUR_SUM_INT_ARRAY", "v1", "INT_ARRAY_WITH_TARGET", "v1",
                List.of(new VariableDefinition("n", "length of nums", "nums", "length")),
                Map.of("n", List.of(64, 128, 256)),
                Map.of("n", 512),
                List.of("RANDOM", "SORTED", "ADVERSARIAL"),
                new MeasurementLimits(3, 5, 1000, 15000));
    }
}
