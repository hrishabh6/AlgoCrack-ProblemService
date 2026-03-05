package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for receiving submission stats from SubmissionService API.
 * Mirrors the SubmissionService's UserSubmissionStatsDto response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubmissionStatsDto {
    private long solvedEasy;
    private long solvedMedium;
    private long solvedHard;

    private List<LanguageStat> languageStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageStat {
        private String language;
        private long count;
    }
}
