package com.hrishabh.problemservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionResponseDto {
    private Long id;
    private String code;
    private String language;
    private String explanation;
    private Long questionId;
}
