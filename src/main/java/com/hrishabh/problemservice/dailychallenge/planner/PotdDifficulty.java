package com.hrishabh.problemservice.dailychallenge.planner;

public enum PotdDifficulty {
    EASY,
    MEDIUM,
    HARD,
    UNKNOWN;

    public static PotdDifficulty fromLevel(String level) {
        if (level == null || level.isBlank()) {
            return UNKNOWN;
        }
        return switch (level.trim().toLowerCase()) {
            case "easy" -> EASY;
            case "medium" -> MEDIUM;
            case "hard" -> HARD;
            default -> UNKNOWN;
        };
    }
}
