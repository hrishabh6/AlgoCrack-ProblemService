package com.hrishabh.problemservice.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.List;

public final class QuestionRankMetadataDtos {

    private QuestionRankMetadataDtos() {
    }

    public record RankMetadataBatchRequest(
            @NotNull @Size(min = 1, max = 200) List<Long> questionIds) {
    }

    @Value
    @Builder
    public static class TagRef {
        long tagId;
        String tagName;
    }

    @Value
    @Builder
    public static class QuestionRankMetadataItem {
        long questionId;
        /** PUBLISHED, DISABLED, DRAFT, or NOT_FOUND when the id is unknown. */
        String questionStatus;
        String difficultyLevel;
        /** EASY, MEDIUM, HARD, or UNKNOWN. */
        String normalizedDifficulty;
        List<TagRef> tags;
    }

    @Value
    @Builder
    public static class RankMetadataBatchResponse {
        List<QuestionRankMetadataItem> items;
    }

    @Value
    @Builder
    public static class PotdResolveResponse {
        boolean matched;
        boolean qualifies;
        Long challengeId;
        String challengeDate;
        Long questionId;
        String challengeStatus;
        String difficultyLevel;
        String normalizedDifficulty;
    }
}
