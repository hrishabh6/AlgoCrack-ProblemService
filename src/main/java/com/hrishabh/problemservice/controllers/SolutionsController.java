package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.CreateSolutionRequestDto;
import com.hrishabh.problemservice.dto.SolutionResponseDto;
import com.hrishabh.problemservice.dto.UpdateSolutionRequestDto;
import com.hrishabh.problemservice.service.SolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solutions")
@RequiredArgsConstructor
public class SolutionsController {

    private final SolutionService solutionService;

    /**
     * Get all solutions for a question
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<SolutionResponseDto>> getSolutionsByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(solutionService.getSolutionsByQuestion(questionId));
    }

    /**
     * Add a new solution to a question
     */
    @PostMapping
    public ResponseEntity<SolutionResponseDto> addSolution(@Valid @RequestBody CreateSolutionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solutionService.addSolution(dto));
    }

    /**
     * Update solution by ID
     */
    @PutMapping("/{solutionId}")
    public ResponseEntity<SolutionResponseDto> updateSolution(
            @PathVariable Long solutionId,
            @Valid @RequestBody UpdateSolutionRequestDto dto) {
        return ResponseEntity.ok(solutionService.updateSolution(solutionId, dto));
    }

    /**
     * Delete solution by ID
     */
    @DeleteMapping("/{solutionId}")
    public ResponseEntity<Void> deleteSolution(@PathVariable Long solutionId) {
        solutionService.deleteSolution(solutionId);
        return ResponseEntity.noContent().build();
    }
}
