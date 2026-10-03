package com.hrishabh.problemservice.dailychallenge.planner;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PotdConfigurationHasher {

    private PotdConfigurationHasher() {
    }

    public static String hash(PotdSchedulerProperties properties) {
        PotdSchedulerProperties.ScoringWeights w = properties.getScoring();
        PotdSchedulerProperties.DifficultyTargets d = properties.getDifficulty();
        String canonical = String.join("|",
                Integer.toString(properties.getExactCooldownDays()),
                Integer.toString(properties.getEmergencyMinCooldownDays()),
                Integer.toString(properties.getFamilyCooldownDays()),
                Double.toString(w.getQuality()),
                Double.toString(w.getDifficultyDeficit()),
                Double.toString(w.getPrimaryTopicDeficit()),
                Double.toString(w.getFreshness()),
                Double.toString(w.getNeverUsed()),
                Double.toString(w.getRecentTagSimilarity()),
                Double.toString(w.getPrimaryTopicRecency()),
                Double.toString(w.getDifficultyStreak()),
                Double.toString(w.getOverTarget()),
                Double.toString(d.getEasyRatio()),
                Double.toString(d.getMediumRatio()),
                Double.toString(d.getHardRatio()),
                Integer.toString(d.getTolerancePer30Days()));
        return sha256Hex(canonical);
    }

    public static String sha256Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
