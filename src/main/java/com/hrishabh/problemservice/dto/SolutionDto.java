package com.hrishabh.problemservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionDto {
    private String code;
    private String language;
}
