package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors SubmissionService's QuestionStatsDto (per-question submission counts).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatsApiDto {
    private Long questionId;
    private long totalSubmissions;
    private long acceptedSubmissions;
}
