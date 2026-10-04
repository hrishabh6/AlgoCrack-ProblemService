package com.hrishabh.problemservice.complexity.internal;

import com.hrishabh.problemservice.dto.ReferenceSolutionDto;
import com.hrishabh.problemservice.service.ReferenceSolutionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal oracle access for future complexity benchmark validation.
 * Public {@code /api/v1/questions/{id}/reference-solution} remains for legacy callers until Gateway denies it.
 */
@RestController
@RequestMapping("/api/v1/internal/questions/{questionId}/reference-solution")
@RequiredArgsConstructor
public class InternalReferenceSolutionController {

    private final ReferenceSolutionService referenceSolutionService;
    private final InternalServiceAuth internalServiceAuth;

    @GetMapping
    public ResponseEntity<ReferenceSolutionDto> getReferenceSolution(
            @PathVariable Long questionId,
            HttpServletRequest request) {
        internalServiceAuth.requireInternal(request);
        return ResponseEntity.ok(referenceSolutionService.getByQuestionId(questionId));
    }
}
