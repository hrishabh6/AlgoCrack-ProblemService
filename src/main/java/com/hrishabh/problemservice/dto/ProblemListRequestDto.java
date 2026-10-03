package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for creating (name required) or updating (null fields are left unchanged) a problem list.
 * An empty description on update clears it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListRequestDto {
    private String name;
    private String description;
}
