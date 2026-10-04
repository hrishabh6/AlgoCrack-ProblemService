package com.hrishabh.problemservice.complexity.support;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Stable seed material: questionId + profileVersion + generatorVersion + sizeVector + variant.
 * Must not include analysisId, clocks, or user-specific data.
 */
public final class DeterministicSeedDeriver {

    private DeterministicSeedDeriver() {
    }

    public static String seedMaterial(
            long questionId,
            String profileVersion,
            String generatorVersion,
            Map<String, Integer> sizeVector,
            String variant) {
        String sizes = new TreeMap<>(sizeVector).entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        return questionId + "|" + profileVersion + "|" + generatorVersion + "|" + sizes + "|" + variant;
    }

    public static String seedValue(String material) {
        return ProfileContentHasher.sha256Hex(material);
    }

    public static long seedLong(String material) {
        String hex = ProfileContentHasher.sha256Hex(material);
        return Long.parseUnsignedLong(hex.substring(0, 16), 16);
    }
}
