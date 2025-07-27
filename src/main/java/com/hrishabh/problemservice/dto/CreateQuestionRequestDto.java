package com.hrishabh.problemservice.dto;

import com.hrishabh.algocrackentityservice.models.Solution;
import com.hrishabh.algocrackentityservice.models.Tag;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequestDto {

    private String questionTitle;
    private String questionDescription;
    private List<TestCaseDto> testCases;
    private Boolean isOutputOrderMatters;
    private List<TagDto> tags;
    private String difficultyLevel;
    private String company;
    private String constraints;
    private Integer timeoutLimit;
    private List<SolutionDto> solution;


}
