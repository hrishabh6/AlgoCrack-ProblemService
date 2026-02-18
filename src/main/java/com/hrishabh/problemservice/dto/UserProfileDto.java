package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private UserDetails userDetails;
    private UserStats userStats;
    private List<LanguageStat> languageStats;
    private List<RecentSubmission> recentSubmissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetails {
        private String userId;
        private String name;
        private String rank;
        private String headline;
        private String about;
        private String location;
        private String school;
        private String website;
        private String githubProfile;
        private String twitterProfile;
        private String linkedinProfile;
        private String skills;
        private String imgUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long totalSolved;
        private long totalQuestions;
        private long easySolved;
        private long easyTotal;
        private long mediumSolved;
        private long mediumTotal;
        private long hardSolved;
        private long hardTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageStat {
        private String language;
        private long problemsSolved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSubmission {
        private String submissionId;
        private String questionTitle;
        private String questionSlug;
        private String verdict;
        private LocalDateTime timestamp;
    }
}
