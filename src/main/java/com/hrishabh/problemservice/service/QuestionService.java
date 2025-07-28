package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.*;
import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
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
    public ResponseEntity<CreateQuestionResponseDto> saveQuestion(QuestionRequestDto dto) {

        // ✅ Validate required fields
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

        if (dto.getMetadataList() == null || dto.getMetadataList().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("At least one metadata entry is required")
                            .build()
            );
        }

        for (QuestionMetadataDto meta : dto.getMetadataList()) {
            if (meta.getFunctionName() == null || meta.getReturnType() == null ||
                    meta.getParamTypes() == null || meta.getParamNames() == null ||
                    meta.getParamTypes().size() != meta.getParamNames().size()) {
                return ResponseEntity.badRequest().body(
                        CreateQuestionResponseDto.builder()
                                .message("Invalid metadata: parameter names/types must be non-null and size must match")
                                .build()
                );
            }
        }

        // ✅ Validate tags exist in DB
        List<Tag> tags = new ArrayList<>();
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (TagDto tagDto : dto.getTags()) {
                Optional<Tag> existing = tagRepository.findByName(tagDto.getName());
                if (existing.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            CreateQuestionResponseDto.builder()
                                    .message("Tag does not exist: " + tagDto.getName())
                                    .build()
                    );
                }
                tags.add(existing.get());
            }
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
                .tags(tags)
                .build();

        // ✅ Map and attach test cases
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

        // ✅ Map and attach solutions
        List<Solution> solutions = dto.getSolution() != null ? dto.getSolution().stream().map(sol ->
                Solution.builder()
                        .code(sol.getCode())
                        .language(sol.getLanguage())
                        .question(question)
                        .build()
        ).collect(Collectors.toList()) : new ArrayList<>();

        question.setSolutions(solutions);

        // ✅ Map and attach metadata
        List<QuestionMetadata> metadataList = dto.getMetadataList().stream().map(md ->
                QuestionMetadata.builder()
                        .functionName(md.getFunctionName())
                        .returnType(md.getReturnType())
                        .paramTypes(md.getParamTypes())
                        .paramNames(md.getParamNames())
                        .language(md.getLanguage())
                        .codeTemplate(md.getCodeTemplate())
                        .executionStrategy(md.getExecutionStrategy())
                        .customInputEnabled(md.getCustomInputEnabled())
                        .question(question)
                        .build()
        ).collect(Collectors.toList());

        question.setMetadataList(metadataList);

        // ✅ Save everything
        Question savedQuestion = questionsRepository.save(question);

        return ResponseEntity.ok(
                CreateQuestionResponseDto.builder()
                        .questionId(savedQuestion.getId())
                        .message("Question created successfully")
                        .build()
        );
    }


    public QuestionResponseDto getQuestionById(Long id) {
        Question question = questionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));

        return QuestionResponseDto.builder()
                .id(question.getId())
                .questionTitle(question.getQuestionTitle())
                .questionDescription(question.getQuestionDescription())
                .isOutputOrderMatters(question.getIsOutputOrderMatters())
                .tags(question.getTags()
                        .stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .difficultyLevel(question.getDifficultyLevel())
                .company(question.getCompany())
                .constraints(question.getConstraints())
                .build();
    }

    public void deleteQuestionById(Long id) {
        if (!questionsRepository.existsById(id)) {
            throw new RuntimeException("Question not found with ID: " + id);
        }
        questionsRepository.deleteById(id);
    }

    public QuestionResponseDto updateQuestion(Long id, QuestionRequestDto dto) {
        Question question = questionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));


        // Update only if field is not null
        if (dto.getQuestionTitle() != null) {
            question.setQuestionTitle(dto.getQuestionTitle());
        }

        if (dto.getQuestionDescription() != null) {
            question.setQuestionDescription(dto.getQuestionDescription());
        }

        if (dto.getIsOutputOrderMatters() != null) {
            question.setIsOutputOrderMatters(dto.getIsOutputOrderMatters());
        }

        if (dto.getDifficultyLevel() != null) {
            question.setDifficultyLevel(dto.getDifficultyLevel());
        }

        if (dto.getCompany() != null) {
            question.setCompany(dto.getCompany());
        }

        if (dto.getConstraints() != null) {
            question.setConstraints(dto.getConstraints());
        }

        if (dto.getTimeoutLimit() != null) {
            question.setTimeoutLimit(dto.getTimeoutLimit());
        }

        // Optional: Handle tags (if tag updating is allowed)
        if (dto.getTags() != null) {
            // Fetch all existing tags in one query
            List<String> tagNames = dto.getTags().stream()
                    .map(TagDto::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.toList());

            List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
            Map<String, Tag> tagMap = existingTags.stream()
                    .collect(Collectors.toMap(Tag::getName, tag -> tag));

            // Create and save new tags in bulk
            List<Tag> newTags = tagNames.stream()
                    .filter(name -> !tagMap.containsKey(name))
                    .map(name -> Tag.builder().name(name).build())
                    .collect(Collectors.toList());

            if (!newTags.isEmpty()) {
                tagRepository.saveAll(newTags);
                newTags.forEach(tag -> tagMap.put(tag.getName(), tag));
            }

            List<Tag> updatedTags = tagNames.stream()
                    .map(tagMap::get)
                    .collect(Collectors.toList());

            question.setTags(updatedTags);
        }

        questionsRepository.save(question);

        // Reuse the same mapper as getQuestionById
        return QuestionResponseDto.builder()
                .id(question.getId())
                .questionTitle(question.getQuestionTitle())
                .questionDescription(question.getQuestionDescription())
                .isOutputOrderMatters(question.getIsOutputOrderMatters())
                .tags(question.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .difficultyLevel(question.getDifficultyLevel())
                .company(question.getCompany())
                .constraints(question.getConstraints())
                .build();
    }

}
