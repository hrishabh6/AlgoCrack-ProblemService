package com.hrishabh.problemservice.client;

import com.hrishabh.problemservice.dto.AcceptedSubmissionDayDto;
import com.hrishabh.problemservice.dto.HeatmapApiDto;
import com.hrishabh.problemservice.dto.QuestionStatsApiDto;
import com.hrishabh.problemservice.dto.StreakDto;
import com.hrishabh.problemservice.dto.SubmissionSummaryApiDto;
import com.hrishabh.problemservice.dto.UserSubmissionStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Fetch distinct accepted solved question IDs for a user.
     */
    public List<Long> getSolvedQuestionIds(String userId) {
        String url = baseUrl + "/api/v1/submissions/stats/" + userId + "/solved-question-ids";
        log.debug("Fetching solved question IDs: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Long>>() {}).getBody();
        } catch (RestClientException e) {
            log.warn("Failed to fetch solved question IDs for {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetch aggregate submission counts for a batch of questions, keyed by question id.
     * Returns an empty map when SubmissionService is unavailable so listings still render.
     */
    public Map<Long, QuestionStatsApiDto> getQuestionStats(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v1/submissions/question-stats")
                .queryParam("ids", questionIds.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .toUriString();
        log.debug("Fetching question stats: {}", url);
        try {
            List<QuestionStatsApiDto> stats = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<QuestionStatsApiDto>>() {}).getBody();
            if (stats == null) {
                return Map.of();
            }
            return stats.stream()
                    .filter(s -> s.getQuestionId() != null)
                    .collect(Collectors.toMap(QuestionStatsApiDto::getQuestionId, s -> s, (a, b) -> a));
        } catch (RestClientException e) {
            log.warn("Failed to fetch question stats for {} questions: {}", questionIds.size(), e.getMessage());
            return Map.of();
        }
    }

    /**
     * Fetch the daily submission streak for a user.
     */
    public StreakDto getStreak(String userId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v1/submissions/stats/{userId}/streak")
                .buildAndExpand(userId)
                .toUriString();
        log.debug("Fetching streak: {}", url);
        return restTemplate.getForObject(url, StreakDto.class);
    }

    /** Fetch accepted problem/date facts so ProblemService can match its POTD schedule. */
    public List<AcceptedSubmissionDayDto> getAcceptedSubmissionDays(
            String userId, LocalDate from, LocalDate to) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v1/submissions/stats/{userId}/accepted-days")
                .queryParam("from", from)
                .queryParam("to", to)
                .buildAndExpand(userId)
                .toUriString();
        log.debug("Fetching accepted submission days: {}", url);
        List<AcceptedSubmissionDayDto> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AcceptedSubmissionDayDto>>() {}).getBody();
        return response == null ? List.of() : response;
    }

    /**
     * Fetch recent submissions for a user.
     */
    public List<SubmissionSummaryApiDto> getUserSubmissions(String userId, int page, int size) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v1/submissions/user/{userId}")
                .queryParam("page", page)
                .queryParam("size", size)
                .buildAndExpand(userId)
                .toUriString();
        log.debug("Fetching submission history: {}", url);

        try {
            return restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<SubmissionSummaryApiDto>>() {})
                    .getBody();
        } catch (RestClientException e) {
            log.warn("Failed to fetch submission history for {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetch heatmap data for a user.
     * SubmissionService requires from/to query params.
     */
    public HeatmapApiDto getHeatmap(String userId, Integer year, String fromParam, String toParam) {
        LocalDate from;
        LocalDate to;

        if (fromParam != null && toParam != null) {
            from = LocalDate.parse(fromParam);
            to = LocalDate.parse(toParam);
        } else if (year != null) {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        } else {
            int currentYear = LocalDate.now().getYear();
            from = LocalDate.of(currentYear, 1, 1);
            to = LocalDate.of(currentYear, 12, 31);
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/v1/submissions/heatmap/{userId}")
                .queryParam("from", from)
                .queryParam("to", to)
                .buildAndExpand(userId)
                .toUriString();

        log.debug("Fetching heatmap data: {}", url);
        HeatmapApiDto response = restTemplate.getForObject(url, HeatmapApiDto.class);

        if (response != null) {
            response.setYear(year != null ? year : from.getYear());
            response.setFrom(from.toString());
            response.setTo(to.toString());
        }

        return response;
    }
}
