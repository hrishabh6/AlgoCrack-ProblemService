package com.hrishabh.problemservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {
    private Long id;
    private String questionTitle;
    private String questionDescription;
    private Boolean isOutputOrderMatters;
    private List<String> tags;
    private String difficultyLevel;
    private String company;
    private String constraints;
}
