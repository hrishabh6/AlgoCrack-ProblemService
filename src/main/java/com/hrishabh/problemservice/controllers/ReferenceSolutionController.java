package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.ReferenceSolutionDto;
import com.hrishabh.problemservice.service.ReferenceSolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions/{questionId}/reference-solution")
@RequiredArgsConstructor
public class ReferenceSolutionController {

    private final ReferenceSolutionService referenceSolutionService;

    /**
     * Get reference solution for a question
     * NOTE: This endpoint should be admin-only in production
     */
    @GetMapping
    public ResponseEntity<ReferenceSolutionDto> getReferenceSolution(@PathVariable Long questionId) {
        ReferenceSolutionDto dto = referenceSolutionService.getByQuestionId(questionId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Create or update reference solution for a question
     * NOTE: This endpoint should be admin-only in production
     */
    @PutMapping
    public ResponseEntity<ReferenceSolutionDto> createOrUpdateReferenceSolution(
            @PathVariable Long questionId,
            @Valid @RequestBody ReferenceSolutionDto dto) {
        ReferenceSolutionDto saved = referenceSolutionService.createOrUpdate(questionId, dto);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete reference solution for a question
     * NOTE: This endpoint should be admin-only in production
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteReferenceSolution(@PathVariable Long questionId) {
        referenceSolutionService.delete(questionId);
        return ResponseEntity.noContent().build();
    }
}
