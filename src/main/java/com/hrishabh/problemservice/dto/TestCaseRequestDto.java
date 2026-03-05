package com.hrishabh.problemservice.dto;

import com.hrishabh.problemservice.models.TestCaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRequestDto {
    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Input is required")
    private String input;

    @NotNull(message = "Type is required")
    private TestCaseType type;
}

