package com.hrishabh.problemservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateQuestionsResponseDto {
    private int totalReceived;
    private int successCount;
    private int failedCount;
    private List<BulkQuestionResult> results;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkQuestionResult {
        private int index;
        private String questionTitle;
        private Long questionId;       // null if failed
        private boolean success;
        private String message;        // Success message or error details
    }
}
