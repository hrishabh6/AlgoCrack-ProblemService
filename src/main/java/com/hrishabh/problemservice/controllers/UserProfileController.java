package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.HeatmapDto;
import com.hrishabh.problemservice.dto.QuestionQuery;
import com.hrishabh.problemservice.dto.QuestionSummaryDto;
import com.hrishabh.problemservice.dto.StreakDto;
import com.hrishabh.problemservice.dto.UserProfileDto;
import com.hrishabh.problemservice.helper.CurrentUser;
import com.hrishabh.problemservice.service.QuestionService;
import com.hrishabh.problemservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final QuestionService questionService;

    /**
     * Ids of problems the caller has an accepted submission for.
     */
    @GetMapping("/me/solved-question-ids")
    public ResponseEntity<List<Long>> getMySolvedQuestionIds(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(userProfileService.getSolvedQuestionIds(CurrentUser.require(userId)));
    }

    /**
     * Same listing as {@code GET /api/v1/questions}, additionally filtered by the caller's solve status.
     *
     * @param status {@code solved} or {@code unsolved}; anything else applies no status filter
     */
    @GetMapping("/me/questions")
    public ResponseEntity<Page<QuestionSummaryDto>> listMyQuestions(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {
        String uid = CurrentUser.require(userId);
        QuestionQuery query = ProblemsController.buildQuery(page, size, difficulty, tag, tags, search, null, sort,
                order);
        if ("solved".equalsIgnoreCase(status) || "unsolved".equalsIgnoreCase(status)) {
            Set<Long> solved = new HashSet<>(userProfileService.getSolvedQuestionIds(uid));
            boolean wantSolved = "solved".equalsIgnoreCase(status);
            return ResponseEntity.ok(questionService.listQuestions(query,
                    wantSolved ? solved : null,
                    wantSolved ? null : solved));
        }
        return ResponseEntity.ok(questionService.listQuestions(query));
    }

    /**
     * Daily submission streak for a user.
     */
    @GetMapping("/streak/{userId}")
    public ResponseEntity<StreakDto> getStreak(@PathVariable String userId) {
        return ResponseEntity.ok(userProfileService.getStreak(userId));
    }

    /**
     * Get user profile with details, stats, language breakdown, and recent submissions.
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching profile for user: {}, page: {}, size: {}", userId, page, size);
        try {
            UserProfileDto profile = userProfileService.getUserProfile(userId, page, size);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.warn("Profile not found for userId={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get submission heatmap for a user.
     *
     * <p>Without {@code year} param: returns last 365 days from today.</p>
     * <p>With {@code year} param (e.g. {@code ?year=2024}): returns full calendar year.</p>
     *
     * @param userId the user's business ID
     * @param year   optional 4-digit year (2000 – current year)
     */
    @GetMapping("/heatmap/{userId}")
    public ResponseEntity<?> getHeatmap(
            @PathVariable String userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        log.info("Fetching heatmap for user: {}, year: {}, from: {}, to: {}", userId, year, from, to);
        try {
            HeatmapDto heatmap = userProfileService.getHeatmap(userId, year, from, to);
            return ResponseEntity.ok(heatmap);
        } catch (IllegalArgumentException e) {
            // Invalid year value
            log.warn("Invalid year param for heatmap userId={}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // User not found
            log.warn("User not found for heatmap userId={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
