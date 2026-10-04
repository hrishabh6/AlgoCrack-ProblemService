package com.hrishabh.problemservice.complexity.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public final class ComplexityProfileDtos {

    private ComplexityProfileDtos() {
    }

    public record VariableDefinition(
            String name,
            String meaning,
            String parameter,
            String dimension) {
    }

    public record ProfileMetadataResponse(
            long questionId,
            String language,
            String profileId,
            String profileCode,
            String profileVersion,
            String profileHash,
            String generatorKey,
            String generatorVersion,
            List<VariableDefinition> variables,
            Map<String, List<Integer>> sizeLadder,
            Map<String, Integer> maxSizes,
            List<String> variants,
            int warmups,
            int measuredRepeats,
            int perInvocationTimeoutMs,
            int maxTotalProfileMs) {
    }

    public record CasesRequest(
            @NotBlank String language,
            @NotBlank String profileVersion,
            @NotBlank String profileHash) {
    }

    public record GeneratedCaseDto(
            String profileCode,
            String profileVersion,
            String profileHash,
            String generatorVersion,
            String caseId,
            String caseIdentity,
            Map<String, Integer> sizeVector,
            String variant,
            String seed,
            String serializedInput,
            JsonNode input,
            String inputHash) {
    }

    public record CasesResponse(
            long questionId,
            String language,
            String profileId,
            String profileCode,
            String profileVersion,
            String profileHash,
            String generatorKey,
            String generatorVersion,
            List<GeneratedCaseDto> cases) {
    }

    public record ProfileUnavailableResponse(
            @NotNull String errorCode,
            String message) {
    }
}
