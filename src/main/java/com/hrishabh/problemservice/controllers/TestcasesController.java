package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.models.TestCaseType;
import com.hrishabh.problemservice.dto.TestCaseRequestDto;
import com.hrishabh.problemservice.dto.TestCaseResponseDto;
import com.hrishabh.problemservice.service.TestcasesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/testcases")
@RequiredArgsConstructor
public class TestcasesController {

    private final TestcasesService testcasesService;

    /**
     * Add a new test case to a question
     */
    @PostMapping
    public ResponseEntity<Void> addTestCase(@Valid @RequestBody TestCaseRequestDto dto) {
        testcasesService.addTestCase(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Get test case by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestCaseResponseDto> getTestCase(@PathVariable Long id) {
        return ResponseEntity.ok(testcasesService.getTestCase(id));
    }

    /**
     * Get all test cases for a question (optionally filtered by type)
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<TestCaseResponseDto>> getTestCasesByQuestion(
            @PathVariable Long questionId,
            @RequestParam(required = false) TestCaseType type) {
        if (type != null) {
            return ResponseEntity.ok(testcasesService.getTestCasesByQuestionAndType(questionId, type));
        }
        return ResponseEntity.ok(testcasesService.getTestCasesByQuestion(questionId));
    }

    /**
     * Update test case by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestCaseResponseDto> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseRequestDto dto) {
        return ResponseEntity.ok(testcasesService.updateTestCase(id, dto));
    }

    /**
     * Delete test case by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id) {
        testcasesService.deleteTestCase(id);
        return ResponseEntity.noContent().build();
    }
}
