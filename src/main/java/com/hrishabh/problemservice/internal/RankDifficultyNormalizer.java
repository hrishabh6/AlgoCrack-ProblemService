package com.hrishabh.problemservice.internal;

public final class RankDifficultyNormalizer {

    private RankDifficultyNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "UNKNOWN";
        }
        return switch (raw.trim().toUpperCase()) {
            case "EASY", "MEDIUM", "HARD" -> raw.trim().toUpperCase();
            default -> "UNKNOWN";
        };
    }
}
