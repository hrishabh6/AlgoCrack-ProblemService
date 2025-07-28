package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.QuestionRequestDto;
import com.hrishabh.problemservice.dto.CreateQuestionResponseDto;
import com.hrishabh.problemservice.dto.QuestionResponseDto;
import com.hrishabh.problemservice.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
public class ProblemsController {

    private final QuestionService questionService;

    @Autowired
    public ProblemsController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<CreateQuestionResponseDto> createQuestion(
            @RequestBody QuestionRequestDto requestDto
    ) {
        return questionService.saveQuestion(requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestionById(@PathVariable Long id) {
        QuestionResponseDto dto = questionService.getQuestionById(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestionById(id);
            return ResponseEntity.ok("Question deleted successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionRequestDto updateDto
    ) {
        QuestionResponseDto updatedQuestion = questionService.updateQuestion(id, updateDto);
        return ResponseEntity.ok(updatedQuestion);
    }


}
