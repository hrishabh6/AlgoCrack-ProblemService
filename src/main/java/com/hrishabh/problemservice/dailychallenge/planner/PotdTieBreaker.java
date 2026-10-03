package com.hrishabh.problemservice.dailychallenge.planner;

import java.time.LocalDate;

public final class PotdTieBreaker {

    private PotdTieBreaker() {
    }

    public static String deterministicSeed(
            String schedulerVersion,
            String configurationHash,
            LocalDate planningStart,
            LocalDate planningEnd,
            LocalDate challengeDate,
            long questionId) {
        String payload = String.join("|",
                schedulerVersion,
                configurationHash,
                planningStart.toString(),
                planningEnd.toString(),
                challengeDate.toString(),
                Long.toString(questionId));
        return PotdConfigurationHasher.sha256Hex(payload);
    }

    /** Ascending tie order: lower hash wins; then lower question id. */
    public static int compareAscending(String hashA, long idA, String hashB, long idB) {
        int hashCompare = unsignedLexicographicCompare(hashA, hashB);
        if (hashCompare != 0) {
            return hashCompare;
        }
        return Long.compare(idA, idB);
    }

    static int unsignedLexicographicCompare(String a, String b) {
        int len = Math.min(a.length(), b.length());
        for (int i = 0; i < len; i++) {
            int ca = a.charAt(i);
            int cb = b.charAt(i);
            if (ca != cb) {
                return Integer.compare(ca, cb);
            }
        }
        return Integer.compare(a.length(), b.length());
    }
}
