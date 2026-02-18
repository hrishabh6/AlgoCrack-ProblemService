package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the heatmap API.
 * Contains metadata about the requested period and a list of active days.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDto {

    /** The year this heatmap covers (null if rolling 365-day mode) */
    private Integer year;

    /** Start date of the range (ISO date string: yyyy-MM-dd) */
    private String from;

    /** End date of the range (ISO date string: yyyy-MM-dd) */
    private String to;

    /** Total submissions in this period */
    private long totalSubmissions;

    /** Total active days (days with at least 1 submission) */
    private int totalActiveDays;

    /** Per-day submission counts — only days with >= 1 submission are included */
    private List<DayActivity> activity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayActivity {
        /** Date in ISO format: yyyy-MM-dd */
        private String date;

        /** Number of submissions made on this day */
        private long count;
    }
}
