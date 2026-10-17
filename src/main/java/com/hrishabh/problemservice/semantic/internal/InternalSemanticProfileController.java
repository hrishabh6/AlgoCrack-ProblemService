package com.hrishabh.problemservice.semantic.internal;

import com.hrishabh.problemservice.complexity.internal.InternalServiceAuth;
import com.hrishabh.problemservice.semantic.service.SemanticProfileService;
import com.hrishabh.problemservice.semantic.service.SemanticProfileService.SemanticProfileRead;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/questions/{questionId}")
@RequiredArgsConstructor
public class InternalSemanticProfileController {

    private final SemanticProfileService semanticProfileService;
    private final InternalServiceAuth internalServiceAuth;

    @GetMapping("/semantic-profile")
    public ResponseEntity<SemanticProfileRead> getSemanticProfile(
            @PathVariable long questionId,
            @RequestParam String language,
            HttpServletRequest request) {
        internalServiceAuth.requireInternal(request);
        SemanticProfileRead read = semanticProfileService.read(questionId, language);
        return ResponseEntity.status(read.httpStatus()).body(read);
    }
}
