package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.*;
import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionsRepository questionsRepository;

    private final TagRepository tagRepository;

    public QuestionService(QuestionsRepository questionsRepository,
                           TagRepository tagRepository) {
        this.questionsRepository = questionsRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public ResponseEntity<CreateQuestionResponseDto> saveQuestion(CreateQuestionRequestDto dto) {
            Map<String, Object> response = new HashMap<>();

        // ✅ Basic validation
        if (dto.getQuestionTitle() == null || dto.getQuestionTitle().isBlank()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("Question title cannot be empty")
                            .build()
            );
        }

        if (dto.getTestCases() == null || dto.getTestCases().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("At least one test case is required")
                            .build()
            );
        }

        // ✅ Create Question
        Question question = Question.builder()
                .questionTitle(dto.getQuestionTitle())
                .questionDescription(dto.getQuestionDescription())
                .isOutputOrderMatters(dto.getIsOutputOrderMatters())
                .difficultyLevel(dto.getDifficultyLevel())
                .company(dto.getCompany())
                .constraints(dto.getConstraints())
                .timeoutLimit(dto.getTimeoutLimit())
                .build();

        // ✅ Map and Attach TestCases
        List<TestCase> testCases = dto.getTestCases().stream().map(tc ->
                TestCase.builder()
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .orderIndex(tc.getOrderIndex())
                        .isHidden(tc.getIsHidden())
                        .question(question)
                        .build()
        ).collect(Collectors.toList());

        question.setTestCases(testCases);

        // ✅ Map and Attach Solutions
        List<Solution> solutions = dto.getSolution() != null ? dto.getSolution().stream().map(sol ->
                Solution.builder()
                        .code(sol.getCode())
                        .language(sol.getLanguage())
                        .question(question)
                        .build()
        ).collect(Collectors.toList()) : new ArrayList<>();

        question.setSolutions(solutions);

        // ✅ Handle Tags (re-use if already exists)
        List<Tag> tags = dto.getTags() != null ? dto.getTags().stream().map(tagDto -> {
            return tagRepository.findByName(tagDto.getName())
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagDto.getName()).build()));
        }).collect(Collectors.toList()) : new ArrayList<>();

        question.setTags(tags);

        // ✅ Save question with cascaded data
        Question savedQuestion = questionsRepository.save(question);

        return ResponseEntity.ok(
                CreateQuestionResponseDto.builder()
                        .questionId(savedQuestion.getId())
                        .message("Question created successfully")
                        .build()
        );
    }
}
