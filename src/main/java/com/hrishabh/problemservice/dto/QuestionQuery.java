package com.hrishabh.problemservice.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Filter, sort and paging options for listing questions.
 *
 * @param tags  problems must carry every listed tag
 * @param sort  one of {@code id}, {@code title}, {@code difficulty}; anything else falls back to {@code id}
 * @param order {@code asc} (default) or {@code desc}
 */
@Value
@Builder
public class QuestionQuery {
    public static final int MAX_PAGE_SIZE = 100;

    String search;
    String difficulty;
    List<String> tags;
    String company;
    String sort;
    String order;
    int page;
    int size;
}
