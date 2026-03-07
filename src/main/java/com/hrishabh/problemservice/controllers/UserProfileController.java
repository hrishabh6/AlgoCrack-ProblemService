package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.HeatmapDto;
import com.hrishabh.problemservice.dto.UserProfileDto;
import com.hrishabh.problemservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

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
