package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user info from AuthService API.
 * Mirrors the AuthService's UserInfoDto response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userId;
    private String name;
    private String email;
    private Long rank;
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
