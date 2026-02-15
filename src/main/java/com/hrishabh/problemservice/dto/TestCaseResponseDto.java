package com.hrishabh.problemservice.dto;

import com.hrishabh.algocrackentityservice.models.TestCaseType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponseDto {
    private Long id;
    private Long questionId;
    private String input;
    private TestCaseType type;
}
