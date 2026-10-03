package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Daily submission streak. Mirrors SubmissionService's StreakDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakDto {
    private int currentStreak;
    private int longestStreak;
    private boolean activeToday;
    private String lastActiveDate;
    private long totalActiveDays;
}
