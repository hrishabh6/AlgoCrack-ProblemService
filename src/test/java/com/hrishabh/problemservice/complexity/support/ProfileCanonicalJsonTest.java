package com.hrishabh.problemservice.complexity.support;

import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileValidator.MeasurementLimits;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProfileCanonicalJsonTest {

    @Test
    void canonicalPayloadExcludesNonSemanticFields() {
        String payload = ProfileCanonicalJson.canonicalPayload(
                10L, "JAVA", "FOUR_SUM_INT_ARRAY", "v1", "INT_ARRAY_WITH_TARGET", "v1",
                List.of(new VariableDefinition("n", "length of nums", "nums", "length")),
                Map.of("n", List.of(64, 128, 256)),
                Map.of("n", 512),
                List.of("RANDOM", "SORTED", "ADVERSARIAL"),
                new MeasurementLimits(3, 5, 1000, 15000));
        assertFalse(payload.contains("profileId"));
        assertFalse(payload.contains("createdAt"));
        assertFalse(payload.contains("ACTIVE"));
        assertEquals(
                ProfileSeededHashTest.fourSumHash(),
                ProfileContentHasher.sha256Hex(payload));
    }

    @Test
    void variableOrderDoesNotChangeHash() {
        List<VariableDefinition> mThenN = List.of(
                new VariableDefinition("m", "columns of heightMap", "heightMap", "cols"),
                new VariableDefinition("n", "rows of heightMap", "heightMap", "rows"));
        List<VariableDefinition> nThenM = List.of(
                new VariableDefinition("n", "rows of heightMap", "heightMap", "rows"),
                new VariableDefinition("m", "columns of heightMap", "heightMap", "cols"));
        MeasurementLimits limits = new MeasurementLimits(3, 5, 2000, 20000);
        Map<String, List<Integer>> ladder = Map.of("n", List.of(8, 16, 32), "m", List.of(8, 16, 32));
        Map<String, Integer> max = Map.of("n", 64, "m", 64);
        List<String> variants = List.of("RANDOM", "FLAT", "PEAK");
        String hash1 = ProfileCanonicalJson.profileHash(
                1L, "JAVA", "TRAP_RAIN_MATRIX", "v1", "INT_MATRIX_ROWS_COLS", "v1",
                mThenN, ladder, max, variants, limits);
        String hash2 = ProfileCanonicalJson.profileHash(
                1L, "JAVA", "TRAP_RAIN_MATRIX", "v1", "INT_MATRIX_ROWS_COLS", "v1",
                nThenM, ladder, max, variants, limits);
        assertEquals(hash1, hash2);
    }
}
