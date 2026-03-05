package com.hrishabh.problemservice.dto;

import com.hrishabh.problemservice.models.TestCaseType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDto {
    @NotBlank(message = "Input is required")
    private String input;

    // Type is optional here - inferred from parent array (defaultTestcases →
    // DEFAULT, hiddenTestcases → HIDDEN)
    private TestCaseType type;
}
