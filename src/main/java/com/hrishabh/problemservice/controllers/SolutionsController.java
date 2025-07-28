package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.SolutionResponseDto;
import com.hrishabh.problemservice.dto.UpdateSolutionRequestDto;
import com.hrishabh.problemservice.service.SolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solution")
public class SolutionsController {

    private final SolutionService solutionService;

    public SolutionsController(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @PutMapping("/{solutionId}")
    public ResponseEntity<SolutionResponseDto> updateSolution(
            @PathVariable Long solutionId,
            @RequestBody UpdateSolutionRequestDto dto
    ) {
        SolutionResponseDto updated = solutionService.updateSolution(solutionId, dto);
        return ResponseEntity.ok(updated);
    }


}
