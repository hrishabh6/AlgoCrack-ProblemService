package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.ReferenceSolutionDto;
import com.hrishabh.problemservice.helper.CurrentUser;
import com.hrishabh.problemservice.service.ReferenceSolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin authoring for reference solutions. Oracle reads use
 * {@code /api/v1/internal/questions/{id}/reference-solution} only.
 */
@RestController
@RequestMapping("/api/v1/questions/{questionId}/reference-solution")
@RequiredArgsConstructor
public class ReferenceSolutionController {

    private final ReferenceSolutionService referenceSolutionService;

    @PutMapping
    public ResponseEntity<ReferenceSolutionDto> createOrUpdateReferenceSolution(
            @PathVariable Long questionId,
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @Valid @RequestBody ReferenceSolutionDto dto) {
        CurrentUser.requireAdmin(userId, role);
        ReferenceSolutionDto saved = referenceSolutionService.createOrUpdate(questionId, dto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteReferenceSolution(
            @PathVariable Long questionId,
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role) {
        CurrentUser.requireAdmin(userId, role);
        referenceSolutionService.delete(questionId);
        return ResponseEntity.noContent().build();
    }
}
