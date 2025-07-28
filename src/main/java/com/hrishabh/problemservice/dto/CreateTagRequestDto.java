package com.hrishabh.problemservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTagRequestDto {
    private String name;
    private String description;
}

