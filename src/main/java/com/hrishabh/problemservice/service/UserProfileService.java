package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.client.AuthServiceClient;
import com.hrishabh.problemservice.client.SubmissionServiceClient;
import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
        // 1. Fetch User from AuthService API
        UserInfoDto user = authServiceClient.getUser(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        // 2. Question Stats (total per difficulty — local to ProblemService)
        long totalEasy = questionsRepository.countByDifficultyLevel("EASY");
        long totalMedium = questionsRepository.countByDifficultyLevel("MEDIUM");
        long totalHard = questionsRepository.countByDifficultyLevel("HARD");
        long totalQuestions = totalEasy + totalMedium + totalHard;

        // 3. Solved Stats from SubmissionService API
        UserSubmissionStatsDto stats = submissionServiceClient.getStats(userId);
        long solvedEasy = stats != null ? stats.getSolvedEasy() : 0;
        long solvedMedium = stats != null ? stats.getSolvedMedium() : 0;
        long solvedHard = stats != null ? stats.getSolvedHard() : 0;
        long totalSolved = solvedEasy + solvedMedium + solvedHard;

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

        // 5. Recent Submissions — fetched from SubmissionService existing endpoint
        // For now, we return an empty list. The frontend calls SubmissionService directly.
        // This avoids adding more API surface area for data that the frontend already fetches.
        List<UserProfileDto.RecentSubmission> recentSubmissions = new ArrayList<>();

        // 6. User Details from AuthService response
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
    public HeatmapDto getHeatmap(String userId, Integer year) {
        // Validate user exists via AuthService
        if (!authServiceClient.userExists(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }

        // Fetch heatmap from SubmissionService API
        HeatmapApiDto apiData = submissionServiceClient.getHeatmap(userId, year);
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
}
