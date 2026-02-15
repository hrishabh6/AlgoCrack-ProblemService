package com.hrishabh.problemservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDto {
    private Long id;
    private String name;
    private String description;
}
