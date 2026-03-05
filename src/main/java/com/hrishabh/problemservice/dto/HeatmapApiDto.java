package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for receiving heatmap data from SubmissionService API.
 * Mirrors the SubmissionService's HeatmapDataDto response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapApiDto {
    private Integer year;
    private String from;
    private String to;
    private long totalSubmissions;
    private int totalActiveDays;

    private List<DayActivity> activity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayActivity {
        private String date;
        private long count;
    }
}
