package com.hrishabh.problemservice.dto;

import lombok.*;

import java.util.List;

/**
 * DTO for exposing question metadata to SubmissionService via API.
 * Flattens QuestionMetadata + parent Question fields into one response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionMetadataApiDto {
    private Long id;
    private Long questionId;
    private String functionName;
    private String returnType;
    private String language;
    private String codeTemplate;
    private String testCaseFormat;
    private String executionStrategy;
    private List<String> paramTypes;
    private List<String> paramNames;
    private String mutationTarget;
    private String serializationStrategy;
    private String questionType;
    // From Question entity (eagerly loaded):
    private Boolean isOutputOrderMatters;
    private String nodeType;
    private String validationHints;
}
