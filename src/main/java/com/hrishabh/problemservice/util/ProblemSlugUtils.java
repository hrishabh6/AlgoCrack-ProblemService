package com.hrishabh.problemservice.util;

import java.util.Locale;

public final class ProblemSlugUtils {

    private ProblemSlugUtils() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
    }

    /** Canonical daily-challenge link used by the frontend problem workspace. */
    public static String dailyChallengePath(String questionTitle, java.time.LocalDate challengeDate) {
        return "/problems/" + slugify(questionTitle) + "/description?envType=daily-question&envId="
                + challengeDate;
    }
}
