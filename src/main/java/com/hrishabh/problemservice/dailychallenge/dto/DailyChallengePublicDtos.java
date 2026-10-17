package com.hrishabh.problemservice.dailychallenge.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class DailyChallengePublicDtos {

    private DailyChallengePublicDtos() {
    }

    @Value
    @Builder
    public static class DailyChallengeResponse {
        long id;
        LocalDate challengeDate;
        Instant startsAt;
        Instant nextResetAt;
        ProblemSummary problem;
    }

    @Value
    @Builder
    public static class ProblemSummary {
        long id;
        String title;
        String difficulty;
        String primaryTopic;
        List<String> tags;
        String href;
    }

    @Value
    @Builder
    public static class DailyChallengeRangeResponse {
        List<DailyChallengeResponse> challenges;
    }

    /**
     * Validates daily-challenge URL context ({@code envType=daily-question}, {@code envId=date})
     * against the published schedule and the loaded problem.
     */
    @Value
    @Builder
    public static class DailyChallengeContextResponse {
        boolean valid;
        LocalDate challengeDate;
        long expectedQuestionId;
        long actualQuestionId;
        /** True when {@code challengeDate} is before today (UTC). */
        boolean historical;
        /** True when {@code challengeDate} equals today (UTC). */
        boolean activeToday;
        String problemTitle;
    }
}
