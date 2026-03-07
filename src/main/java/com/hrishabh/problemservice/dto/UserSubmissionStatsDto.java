package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonAlias;

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
    @JsonAlias({"totalSolved", "solvedTotal"})
    private long totalSolved;
    @JsonAlias({"easySolved", "solvedEasy"})
    private long easySolved;
    @JsonAlias({"mediumSolved", "solvedMedium"})
    private long mediumSolved;
    @JsonAlias({"hardSolved", "solvedHard"})
    private long hardSolved;

    private List<LanguageStat> languageStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageStat {
        private String language;
        private long count;
    }
}
