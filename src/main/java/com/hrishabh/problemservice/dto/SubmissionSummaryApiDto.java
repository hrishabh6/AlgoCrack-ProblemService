package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Minimal shape of SubmissionService /user/{userId} response needed for profile view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionSummaryApiDto {
    private String submissionId;
    private Long questionId;
    private String verdict;
    private LocalDateTime queuedAt;
    private LocalDateTime completedAt;
}

