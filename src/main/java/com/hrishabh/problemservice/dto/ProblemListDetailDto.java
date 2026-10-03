package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * A list (or the built-in Saved collection, where {@code builtIn} is true and {@code id} is null)
 * with its problems, most recently added first.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListDetailDto {
    private Long id;
    private String name;
    private String description;
    private boolean builtIn;
    private int problemCount;
    private Date createdAt;
    private Date updatedAt;
    private List<QuestionSummaryDto> problems;
}
