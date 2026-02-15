package com.hrishabh.problemservice.dto;

import com.hrishabh.algocrackentityservice.models.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceSolutionDto {
    
    @NotNull(message = "Language is required")
    private Language language;
    
    @NotBlank(message = "Source code is required")
    private String sourceCode;
}
