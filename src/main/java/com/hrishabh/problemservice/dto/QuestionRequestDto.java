package com.hrishabh.problemservice.dto;

import com.hrishabh.problemservice.models.NodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {

    @NotBlank(message = "Question title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String questionTitle;

    @NotBlank(message = "Question description is required")
    private String questionDescription;

    @NotEmpty(message = "At least one default test case is required")
    @Valid
    private List<TestCaseDto> defaultTestcases;

    @Valid
    private List<TestCaseDto> hiddenTestcases;

    @NotEmpty(message = "At least one metadata entry is required")
    @Valid
    private List<QuestionMetadataDto> metadataList;

    @Valid
    private ReferenceSolutionDto referenceSolution;

    private NodeType nodeType;

    private Boolean isOutputOrderMatters;

    private List<TagDto> tags;

    @Pattern(regexp = "^(Easy|Medium|Hard)$", message = "Difficulty must be Easy, Medium, or Hard")
    private String difficultyLevel;

    private String company;
    private String constraints;

    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 30, message = "Timeout must not exceed 30 seconds")
    private Integer timeoutLimit;

    private List<SolutionDto> solution;
}
