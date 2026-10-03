package com.hrishabh.problemservice.dailychallenge.dto;

import com.hrishabh.problemservice.dailychallenge.model.PotdCurationStatus;
import com.hrishabh.problemservice.dailychallenge.model.PotdValidationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

public final class PotdMetadataAdminDtos {

    private PotdMetadataAdminDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpsertMetadataRequest {
        private boolean eligible;
        @NotNull private PotdCurationStatus curationStatus;
        @Min(0) @Max(100) private int qualityScore;
        private Long primaryTagId;
        private String familyKey;
        private Integer cooldownDaysOverride;
        @NotNull private PotdValidationStatus validationStatus;
    }

    @Value
    @Builder
    public static class MetadataResponse {
        long questionId;
        boolean eligible;
        PotdCurationStatus curationStatus;
        int qualityScore;
        Long primaryTagId;
        String familyKey;
        Integer cooldownDaysOverride;
        PotdValidationStatus validationStatus;
    }
}
