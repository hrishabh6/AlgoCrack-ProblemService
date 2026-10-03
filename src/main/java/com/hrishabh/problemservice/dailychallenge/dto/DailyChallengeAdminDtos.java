package com.hrishabh.problemservice.dailychallenge.dto;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeSelectionType;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DailyChallengeAdminDtos {

    private DailyChallengeAdminDtos() {
    }

    @Value
    @Builder
    public static class AdminChallengeRow {
        long id;
        LocalDate challengeDate;
        DailyChallengeStatus status;
        DailyChallengeSelectionType selectionType;
        boolean locked;
        long questionId;
        String questionTitle;
        BigDecimal selectionScore;
        String schedulerVersion;
        String configurationHash;
        long version;
    }

    @Value
    @Builder
    public static class AdminChallengeListResponse {
        List<AdminChallengeRow> challenges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationRequest {
        @NotNull private LocalDate from;
        @NotNull private LocalDate to;
        private boolean dryRun;
        private boolean replaceUnlockedDrafts;
    }

    @Value
    @Builder
    public static class PlannedRowDto {
        LocalDate challengeDate;
        long questionId;
        BigDecimal selectionScore;
    }

    @Value
    @Builder
    public static class GenerationResponse {
        boolean dryRun;
        String configurationHash;
        List<PlannedRowDto> planned;
        int persistedCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicationRequest {
        @NotNull private LocalDate from;
        @NotNull private LocalDate to;
        @NotBlank private String reason;
    }

    @Value
    @Builder
    public static class PublicationResponse {
        int publishedCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualReplaceRequest {
        @NotNull private Long questionId;
        @NotBlank private String reason;
        private Long expectedVersion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasonRequest {
        @NotBlank private String reason;
    }

    @Value
    @Builder
    public static class PreflightResponse {
        int eligibleQuestionCount;
        int easyCount;
        int mediumCount;
        int hardCount;
        int draftHorizonDays;
        int exactCooldownDays;
        boolean sufficientForHorizon;
        String note;
    }

    @Value
    @Builder
    public static class AuditEntryDto {
        long id;
        LocalDate challengeDate;
        String action;
        Long oldQuestionId;
        Long newQuestionId;
        String actor;
        String reason;
        String createdAt;
    }

    @Value
    @Builder
    public static class AuditListResponse {
        List<AuditEntryDto> entries;
    }
}
