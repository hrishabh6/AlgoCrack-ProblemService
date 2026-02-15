package com.hrishabh.problemservice.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryDto {
    private Long id;
    private String questionTitle;
    private String difficultyLevel;
    private List<String> tags;
    private String company;

    // From QuestionStatistics (optional, populated if available)
    private Double acceptanceRate;
    private Integer totalSubmissions;
}
