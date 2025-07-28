package com.hrishabh.problemservice.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRequestDto {
    private Long questionId;
    private String input;
    private String expectedOutput;
    private Integer orderIndex;
    private Boolean isHidden;
}
