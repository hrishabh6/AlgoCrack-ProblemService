package com.hrishabh.problemservice.client;

import com.hrishabh.problemservice.dto.UserInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for AuthService API calls.
 * Replaces the deleted UserRepository.
 */
@Slf4j
@Component
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthServiceClient(RestTemplate restTemplate,
                             @Value("${services.auth.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetch user info by userId.
     * Replaces: UserRepository.findByUserId()
     */
    public UserInfoDto getUser(String userId) {
        String url = baseUrl + "/api/v1/auth/users/" + userId;
        log.debug("Fetching user from AuthService: {}", url);
        return restTemplate.getForObject(url, UserInfoDto.class);
    }

    /**
     * Check if user exists.
     * Replaces: UserRepository.existsByUserId()
     */
    public boolean userExists(String userId) {
        String url = baseUrl + "/api/v1/auth/users/" + userId + "/exists";
        log.debug("Checking user existence in AuthService: {}", url);
        Boolean exists = restTemplate.getForObject(url, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
