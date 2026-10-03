package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Everything the problems page needs about the user's collections in one response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListsOverviewDto {
    private SavedCollection saved;
    private List<ProblemListSummaryDto> lists;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedCollection {
        private int problemCount;
        private List<Long> problemIds;
    }
}
