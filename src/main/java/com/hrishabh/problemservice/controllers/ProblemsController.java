package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.CreateQuestionRequestDto;
import com.hrishabh.problemservice.dto.CreateQuestionResponseDto;
import com.hrishabh.problemservice.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestBody CreateQuestionRequestDto requestDto
    ) {
        return questionService.saveQuestion(requestDto);
    }
}
