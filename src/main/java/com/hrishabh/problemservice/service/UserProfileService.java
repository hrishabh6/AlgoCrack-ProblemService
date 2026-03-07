package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.client.AuthServiceClient;
import com.hrishabh.problemservice.client.SubmissionServiceClient;
import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {

    private final AuthServiceClient authServiceClient;
    private final SubmissionServiceClient submissionServiceClient;
    private final QuestionsRepository questionsRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String userId, int page, int size) {
        // 1. Fetch User from AuthService API. If AuthService is unavailable,
        // continue with fallback user details so profile page can still load stats.
        UserInfoDto user;
        try {
            user = authServiceClient.getUser(userId);
        } catch (RuntimeException e) {
            log.warn("AuthService unavailable while fetching profile user {}: {}. Using fallback user details.",
                    userId, e.getMessage());
            user = null;
        }

        if (user == null) {
            user = UserInfoDto.builder()
                    .userId(userId)
                    .name(userId)
                    .build();
        }

        // 2. Question Stats (total per difficulty — local to ProblemService)
        long totalEasy = questionsRepository.countByDifficultyLevel("EASY");
        long totalMedium = questionsRepository.countByDifficultyLevel("MEDIUM");
        long totalHard = questionsRepository.countByDifficultyLevel("HARD");
        long totalQuestions = totalEasy + totalMedium + totalHard;

        // 3. Solved Stats from SubmissionService API
        UserSubmissionStatsDto stats = submissionServiceClient.getStats(userId);
        long solvedEasy = 0;
        long solvedMedium = 0;
        long solvedHard = 0;
        long totalSolved = 0;

        // Prefer deriving difficulty breakdown from actual solved question IDs.
        List<Long> solvedQuestionIds = submissionServiceClient.getSolvedQuestionIds(userId);
        if (solvedQuestionIds != null && !solvedQuestionIds.isEmpty()) {
            Set<Long> uniqueIds = solvedQuestionIds.stream().collect(Collectors.toSet());
            totalSolved = uniqueIds.size();

            List<Question> solvedQuestions = questionsRepository.findAllById(uniqueIds);
            for (Question q : solvedQuestions) {
                String difficulty = q.getDifficultyLevel() != null
                        ? q.getDifficultyLevel().toUpperCase(Locale.ROOT).trim()
                        : "";
                switch (difficulty) {
                    case "EASY" -> solvedEasy++;
                    case "MEDIUM" -> solvedMedium++;
                    case "HARD" -> solvedHard++;
                    default -> {
                        // Unknown difficulty in question row; keep only total count.
                    }
                }
            }
        } else if (stats != null) {
            // Fallback to stats endpoint breakdown if solved-question endpoint is unavailable.
            solvedEasy = stats.getEasySolved();
            solvedMedium = stats.getMediumSolved();
            solvedHard = stats.getHardSolved();
            totalSolved = stats.getTotalSolved() > 0
                    ? stats.getTotalSolved()
                    : solvedEasy + solvedMedium + solvedHard;
        }

        UserProfileDto.UserStats userStats = UserProfileDto.UserStats.builder()
                .totalSolved(totalSolved)
                .totalQuestions(totalQuestions)
                .easySolved(solvedEasy)
                .easyTotal(totalEasy)
                .mediumSolved(solvedMedium)
                .mediumTotal(totalMedium)
                .hardSolved(solvedHard)
                .hardTotal(totalHard)
                .build();

        // 4. Language Stats from SubmissionService API
        List<UserProfileDto.LanguageStat> languageStats = new ArrayList<>();
        if (stats != null && stats.getLanguageStats() != null) {
            languageStats = stats.getLanguageStats().stream()
                    .map(ls -> new UserProfileDto.LanguageStat(ls.getLanguage(), ls.getCount()))
                    .collect(Collectors.toList());
        }

        // 5. Recent submissions from SubmissionService
        List<UserProfileDto.RecentSubmission> recentSubmissions = submissionServiceClient
                .getUserSubmissions(userId, page, size)
                .stream()
                .map(sub -> {
                    String title = "Problem #" + sub.getQuestionId();
                    String slug = "problem-" + sub.getQuestionId();

                    if (sub.getQuestionId() != null) {
                        Question q = questionsRepository.findById(sub.getQuestionId()).orElse(null);
                        if (q != null && q.getQuestionTitle() != null && !q.getQuestionTitle().isBlank()) {
                            title = q.getQuestionTitle();
                            slug = slugify(title);
                        }
                    }

                    return UserProfileDto.RecentSubmission.builder()
                            .submissionId(sub.getSubmissionId())
                            .questionId(sub.getQuestionId())
                            .questionTitle(title)
                            .questionSlug(slug)
                            .verdict(sub.getVerdict())
                            .timestamp(sub.getCompletedAt() != null ? sub.getCompletedAt() : sub.getQueuedAt())
                            .build();
                })
                .collect(Collectors.toList());

        // 6. User Details from AuthService response (or fallback)
        UserProfileDto.UserDetails userDetails = UserProfileDto.UserDetails.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .rank(user.getRank() != null ? String.valueOf(user.getRank()) : null)
                .headline(user.getHeadline())
                .about(user.getAbout())
                .location(user.getLocation())
                .school(user.getSchool())
                .website(user.getWebsite())
                .githubProfile(user.getGithubProfile())
                .twitterProfile(user.getTwitterProfile())
                .linkedinProfile(user.getLinkedinProfile())
                .skills(user.getSkills())
                .imgUrl(user.getImgUrl())
                .build();

        return UserProfileDto.builder()
                .userDetails(userDetails)
                .userStats(userStats)
                .languageStats(languageStats)
                .recentSubmissions(recentSubmissions)
                .build();
    }

    /**
     * Returns heatmap data for a user via SubmissionService API.
     */
    @Transactional(readOnly = true)
    public HeatmapDto getHeatmap(String userId, Integer year, String from, String to) {
        // Validate user via AuthService when available.
        // If AuthService is unavailable, continue and rely on SubmissionService.
        Boolean userExists = null;
        try {
            userExists = authServiceClient.userExists(userId);
        } catch (RuntimeException e) {
            log.warn("AuthService unavailable while validating user {} for heatmap: {}. Continuing without user check.",
                    userId, e.getMessage());
        }

        if (Boolean.FALSE.equals(userExists)) {
            throw new RuntimeException("User not found: " + userId);
        }

        // Fetch heatmap from SubmissionService API
        HeatmapApiDto apiData = submissionServiceClient.getHeatmap(userId, year, from, to);
        if (apiData == null) {
            return HeatmapDto.builder()
                    .year(year)
                    .totalSubmissions(0)
                    .totalActiveDays(0)
                    .activity(new ArrayList<>())
                    .build();
        }

        // Map API response to internal DTO
        List<HeatmapDto.DayActivity> activity = apiData.getActivity() != null
                ? apiData.getActivity().stream()
                .map(d -> HeatmapDto.DayActivity.builder()
                        .date(d.getDate())
                        .count(d.getCount())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return HeatmapDto.builder()
                .year(apiData.getYear())
                .from(apiData.getFrom())
                .to(apiData.getTo())
                .totalSubmissions(apiData.getTotalSubmissions())
                .totalActiveDays(apiData.getTotalActiveDays())
                .activity(activity)
                .build();
    }

    private String slugify(String input) {
        return input
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
    }
}
