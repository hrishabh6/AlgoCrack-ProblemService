package com.hrishabh.problemservice.client;

import com.hrishabh.problemservice.dto.UserSubmissionStatsDto;
import com.hrishabh.problemservice.dto.HeatmapApiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for SubmissionService API calls.
 * Replaces the deleted SubmissionRepository.
 */
@Slf4j
@Component
public class SubmissionServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SubmissionServiceClient(RestTemplate restTemplate,
                                   @Value("${services.submission.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetch submission stats for a user.
     * Replaces: SubmissionRepository.countDistinctSolvedByUserIdAndDifficulty() etc.
     */
    public UserSubmissionStatsDto getStats(String userId) {
        String url = baseUrl + "/api/v1/submissions/stats/" + userId;
        log.debug("Fetching submission stats: {}", url);
        return restTemplate.getForObject(url, UserSubmissionStatsDto.class);
    }

    /**
     * Fetch heatmap data for a user.
     * Replaces: SubmissionRepository.countSubmissionsGroupedByDateBetween()
     */
    public HeatmapApiDto getHeatmap(String userId, Integer year) {
        String url = baseUrl + "/api/v1/submissions/heatmap/" + userId;
        if (year != null) {
            url += "?year=" + year;
        }
        log.debug("Fetching heatmap data: {}", url);
        return restTemplate.getForObject(url, HeatmapApiDto.class);
    }
}
