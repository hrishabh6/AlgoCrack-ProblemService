package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class ProblemsController {

    private final QuestionService questionService;

    /**
     * List questions with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<QuestionSummaryDto>> listQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String company) {
        Page<QuestionSummaryDto> questions = questionService.listQuestions(page, size, difficulty, tag, search,
                company);
        return ResponseEntity.ok(questions);
    }

    /**
     * Create a new question
     */
    @PostMapping
    public ResponseEntity<CreateQuestionResponseDto> createQuestion(
            @Valid @RequestBody QuestionRequestDto requestDto) {
        return questionService.saveQuestion(requestDto);
    }

    /**
     * Bulk create questions
     * Each question is processed independently - failures don't affect other
     * questions.
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateQuestionsResponseDto> bulkCreateQuestions(
            @RequestBody List<QuestionRequestDto> questions) {
        BulkCreateQuestionsResponseDto response = questionService.bulkSaveQuestions(questions);
        return ResponseEntity.ok(response);
    }

    /**
     * Get question by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestionById(@PathVariable Long id) {
        QuestionResponseDto dto = questionService.getQuestionById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Delete question by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestionById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update question by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDto updateDto) {
        QuestionResponseDto updatedQuestion = questionService.updateQuestion(id, updateDto);
        return ResponseEntity.ok(updatedQuestion);
    }
}
