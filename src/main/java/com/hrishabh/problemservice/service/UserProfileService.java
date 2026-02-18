package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Submission;
import com.hrishabh.algocrackentityservice.models.User;
import com.hrishabh.problemservice.dto.HeatmapDto;
import com.hrishabh.problemservice.dto.UserProfileDto;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.SubmissionRepository;
import com.hrishabh.problemservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final QuestionsRepository questionsRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String userId, int page, int size) {
        // 1. Fetch User
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Question Stats (total per difficulty)
        long totalEasy = questionsRepository.countByDifficultyLevel("EASY");
        long totalMedium = questionsRepository.countByDifficultyLevel("MEDIUM");
        long totalHard = questionsRepository.countByDifficultyLevel("HARD");
        long totalQuestions = totalEasy + totalMedium + totalHard;

        // 3. Solved Stats (unique problems accepted per difficulty)
        long solvedEasy = submissionRepository.countDistinctSolvedByUserIdAndDifficulty(userId, "EASY");
        long solvedMedium = submissionRepository.countDistinctSolvedByUserIdAndDifficulty(userId, "MEDIUM");
        long solvedHard = submissionRepository.countDistinctSolvedByUserIdAndDifficulty(userId, "HARD");
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

        // 4. Language Stats
        List<Object[]> langStatsRaw = submissionRepository.countDistinctSolvedByUserIdGroupByLanguage(userId);
        List<UserProfileDto.LanguageStat> languageStats = new ArrayList<>();
        for (Object[] row : langStatsRaw) {
            String lang = (String) row[0];
            Long count = (Long) row[1];
            languageStats.add(new UserProfileDto.LanguageStat(lang, count));
        }

        // 5. Recent Submissions (paginated)
        Pageable pageable = PageRequest.of(page, size);
        Page<Submission> recentPage = submissionRepository.findByUser_UserIdOrderByQueuedAtDesc(userId, pageable);

        List<UserProfileDto.RecentSubmission> recentSubmissions = recentPage.getContent().stream()
                .map(s -> UserProfileDto.RecentSubmission.builder()
                        .submissionId(s.getSubmissionId())
                        .questionTitle(s.getQuestion() != null ? s.getQuestion().getQuestionTitle() : null)
                        .questionSlug(deriveSlug(s.getQuestion() != null ? s.getQuestion().getQuestionTitle() : null))
                        .verdict(s.getVerdict() != null ? s.getVerdict().name() : "PENDING")
                        .timestamp(s.getCompletedAt() != null ? s.getCompletedAt() : s.getQueuedAt())
                        .build())
                .collect(Collectors.toList());

        // 6. User Details
        UserProfileDto.UserDetails userDetails = UserProfileDto.UserDetails.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .rank(user.getRank())
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

    private String deriveSlug(String title) {
        if (title == null)
            return "";
        return title.toLowerCase().trim().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
    }

    /**
     * Returns heatmap data for a user.
     *
     * @param userId the user's business ID
     * @param year   optional year (e.g. 2024). If null, returns last 365 days from today.
     */
    @Transactional(readOnly = true)
    public HeatmapDto getHeatmap(String userId, Integer year) {
        // Validate user exists
        if (!userRepository.existsByUserId(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }

        LocalDateTime from;
        LocalDateTime to;
        int currentYear = LocalDate.now().getYear();

        if (year != null) {
            // Validate year range — reject obviously invalid years
            if (year < 2000 || year > currentYear) {
                throw new IllegalArgumentException(
                        "Invalid year: " + year + ". Must be between 2000 and " + currentYear + ".");
            }
            from = LocalDate.of(year, 1, 1).atStartOfDay();
            to   = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        } else {
            // Default: rolling last 365 days (today inclusive)
            to   = LocalDate.now().atTime(23, 59, 59);
            from = LocalDate.now().minusDays(364).atStartOfDay();
        }

        List<Object[]> raw = submissionRepository.countSubmissionsGroupedByDateBetween(userId, from, to);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<HeatmapDto.DayActivity> activity = new ArrayList<>();
        long totalSubmissions = 0;

        for (Object[] row : raw) {
            // row[0] is java.sql.Date from FUNCTION('DATE', ...), row[1] is Long count
            String date = row[0].toString(); // java.sql.Date.toString() → "yyyy-MM-dd"
            long count  = ((Number) row[1]).longValue();
            activity.add(HeatmapDto.DayActivity.builder()
                    .date(date)
                    .count(count)
                    .build());
            totalSubmissions += count;
        }

        return HeatmapDto.builder()
                .year(year)
                .from(from.toLocalDate().format(fmt))
                .to(to.toLocalDate().format(fmt))
                .totalSubmissions(totalSubmissions)
                .totalActiveDays(activity.size())
                .activity(activity)
                .build();
    }
}
