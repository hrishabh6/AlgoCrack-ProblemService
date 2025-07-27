package com.hrishabh.problemservice.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionResponseDto {
    private Long questionId;
    private String message;
}

