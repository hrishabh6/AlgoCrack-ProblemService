package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * A user's list with its member problem ids (most recently added first), so clients can render
 * membership for every problem row without per-row requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListSummaryDto {
    private Long id;
    private String name;
    private String description;
    private int problemCount;
    private List<Long> problemIds;
    private Date createdAt;
    private Date updatedAt;
}
