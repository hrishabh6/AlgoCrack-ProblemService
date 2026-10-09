package com.hrishabh.problemservice.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Filter, sort and paging options for listing questions.
 *
 * @param tags               problems must carry at least one listed tag
 * @param excludeDifficulty  problems must not have this difficulty
 * @param excludeTags        problems must carry none of these tags
 * @param sort               one of {@code id}, {@code title}, {@code difficulty}; anything else falls back to {@code id}
 * @param order              {@code asc} (default) or {@code desc}
 */
@Value
@Builder
public class QuestionQuery {
    public static final int MAX_PAGE_SIZE = 100;

    String search;
    String difficulty;
    List<String> tags;
    String excludeDifficulty;
    List<String> excludeTags;
    String company;
    String sort;
    String order;
    int page;
    int size;
}
