package com.hrishabh.problemservice.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSolutionRequestDto {
    private String code;
    private String language;
    private String explanation; // optional, if applicable
}
