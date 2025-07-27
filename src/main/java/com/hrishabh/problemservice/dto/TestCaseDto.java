package com.hrishabh.problemservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDto {
    private String input;
    private String expectedOutput;
    private Integer orderIndex;
    private Boolean isHidden;
}
