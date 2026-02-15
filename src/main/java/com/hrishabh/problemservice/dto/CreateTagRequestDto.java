package com.hrishabh.problemservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTagRequestDto {
    @NotBlank(message = "Tag name is required")
    private String name;
    
    private String description;
}
