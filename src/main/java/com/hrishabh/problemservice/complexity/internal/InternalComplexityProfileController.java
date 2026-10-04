package com.hrishabh.problemservice.complexity.internal;

import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.CasesRequest;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.CasesResponse;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.ProfileMetadataResponse;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.ProfileUnavailableResponse;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileService;
import com.hrishabh.problemservice.complexity.service.ProfileUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/questions/{questionId}")
@ConditionalOnProperty(prefix = "complexity", name = "profile-api-enabled", havingValue = "true")
@RequiredArgsConstructor
public class InternalComplexityProfileController {

    private final ComplexityProfileService profileService;
    private final InternalServiceAuth internalServiceAuth;

    @GetMapping("/complexity-profile")
    public ResponseEntity<?> getProfile(
            @PathVariable long questionId,
            @RequestParam String language,
            HttpServletRequest request) {
        internalServiceAuth.requireInternal(request);
        return profileService.getActiveProfile(questionId, language)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        new ProfileUnavailableResponse("PROFILE_UNAVAILABLE", "No active benchmark profile")));
    }

    @PostMapping("/complexity-profile/cases")
    public ResponseEntity<CasesResponse> generateCases(
            @PathVariable long questionId,
            @Valid @RequestBody CasesRequest body,
            HttpServletRequest request) {
        internalServiceAuth.requireInternal(request);
        try {
            return ResponseEntity.ok(profileService.generateCases(questionId, body));
        } catch (ProfileUnavailableException ex) {
            throw ex;
        }
    }
}
