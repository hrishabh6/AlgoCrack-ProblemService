package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.*;
import com.hrishabh.problemservice.dto.*;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionsRepository questionsRepository;
    private final TagRepository tagRepository;
    private final ReferenceSolutionRepository referenceSolutionRepository;

    /**
     * List questions with pagination and filtering
     */
    public Page<QuestionSummaryDto> listQuestions(int page, int size, String difficulty, String tag, String search,
            String company) {
        Specification<Question> spec = Specification
                .where(QuestionSpecification.hasDifficulty(difficulty))
                .and(QuestionSpecification.hasTag(tag))
                .and(QuestionSpecification.titleContains(search))
                .and(QuestionSpecification.hasCompany(company));

        Page<Question> questions = questionsRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("id").descending()));

        return questions.map(q -> QuestionSummaryDto.builder()
                .id(q.getId())
                .questionTitle(q.getQuestionTitle())
                .difficultyLevel(q.getDifficultyLevel())
                .tags(q.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .company(q.getCompany())
                .build());
    }

    @Transactional
    public ResponseEntity<CreateQuestionResponseDto> saveQuestion(QuestionRequestDto dto) {

        // ✅ Validate required fields
        if (dto.getQuestionTitle() == null || dto.getQuestionTitle().isBlank()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("Question title cannot be empty")
                            .build());
        }

        if (dto.getDefaultTestcases() == null || dto.getDefaultTestcases().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("At least one default test case is required")
                            .build());
        }

        if (dto.getMetadataList() == null || dto.getMetadataList().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CreateQuestionResponseDto.builder()
                            .message("At least one metadata entry is required")
                            .build());
        }

        for (QuestionMetadataDto meta : dto.getMetadataList()) {
            if (meta.getFunctionName() == null || meta.getReturnType() == null ||
                    meta.getParamTypes() == null || meta.getParamNames() == null ||
                    meta.getParamTypes().size() != meta.getParamNames().size()) {
                return ResponseEntity.badRequest().body(
                        CreateQuestionResponseDto.builder()
                                .message("Invalid metadata: parameter names/types must be non-null and size must match")
                                .build());
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
                                    .build());
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
                .nodeType(dto.getNodeType())
                .tags(tags)
                .build();

        // ✅ Map and attach DEFAULT test cases
        List<TestCase> testCases = new ArrayList<>();

        for (TestCaseDto tc : dto.getDefaultTestcases()) {
            testCases.add(TestCase.builder()
                    .input(tc.getInput())
                    .type(TestCaseType.DEFAULT)
                    .question(question)
                    .build());
        }

        // ✅ Map and attach HIDDEN test cases
        if (dto.getHiddenTestcases() != null) {
            for (TestCaseDto tc : dto.getHiddenTestcases()) {
                testCases.add(TestCase.builder()
                        .input(tc.getInput())
                        .type(TestCaseType.HIDDEN)
                        .question(question)
                        .build());
            }
        }

        question.setTestCases(testCases);

        // ✅ Map and attach solutions
        List<Solution> solutions = dto.getSolution() != null ? dto.getSolution().stream().map(sol -> Solution.builder()
                .code(sol.getCode())
                .language(sol.getLanguage())
                .question(question)
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        question.setSolutions(solutions);

        // ✅ Map and attach metadata
        List<QuestionMetadata> metadataList = dto.getMetadataList().stream().map(md -> QuestionMetadata.builder()
                .functionName(md.getFunctionName())
                .returnType(md.getReturnType())
                .paramTypes(md.getParamTypes())
                .paramNames(md.getParamNames())
                .language(md.getLanguage())
                .codeTemplate(md.getCodeTemplate())
                .executionStrategy(md.getExecutionStrategy())
                .customInputEnabled(md.getCustomInputEnabled())
                .question(question)
                .build()).collect(Collectors.toList());

        question.setMetadataList(metadataList);

        // ✅ Save question first
        Question savedQuestion = questionsRepository.save(question);

        // ✅ Create and attach reference solution if provided
        if (dto.getReferenceSolution() != null) {
            ReferenceSolution refSol = ReferenceSolution.builder()
                    .question(savedQuestion)
                    .language(dto.getReferenceSolution().getLanguage())
                    .sourceCode(dto.getReferenceSolution().getSourceCode())
                    .build();
            referenceSolutionRepository.save(refSol);
        }

        return ResponseEntity.ok(
                CreateQuestionResponseDto.builder()
                        .questionId(savedQuestion.getId())
                        .message("Question created successfully")
                        .build());
    }

    public QuestionResponseDto getQuestionById(Long id) {
        Question question = questionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));

        // Only return DEFAULT testcases (never return HIDDEN)
        List<TestCaseResponseDto> defaultTestcases = question.getTestCases().stream()
                .filter(tc -> tc.getType() == TestCaseType.DEFAULT)
                .map(tc -> TestCaseResponseDto.builder()
                        .id(tc.getId())
                        .questionId(question.getId())
                        .input(tc.getInput())
                        .type(tc.getType())
                        .build())
                .collect(Collectors.toList());

        // Map metadata
        List<QuestionMetadataDto> metadataList = question.getMetadataList().stream()
                .map(md -> QuestionMetadataDto.builder()
                        .functionName(md.getFunctionName())
                        .returnType(md.getReturnType())
                        .paramTypes(md.getParamTypes())
                        .paramNames(md.getParamNames())
                        .language(md.getLanguage())
                        .codeTemplate(md.getCodeTemplate())
                        .executionStrategy(md.getExecutionStrategy())
                        .customInputEnabled(md.getCustomInputEnabled())
                        .build())
                .collect(Collectors.toList());

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
                .timeoutLimit(question.getTimeoutLimit())
                .nodeType(question.getNodeType())
                .defaultTestcases(defaultTestcases)
                .metadataList(metadataList)
                .build();
    }

    public void deleteQuestionById(Long id) {
        if (!questionsRepository.existsById(id)) {
            throw new RuntimeException("Question not found with ID: " + id);
        }
        questionsRepository.deleteById(id);
    }

    @Transactional
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

        if (dto.getNodeType() != null) {
            question.setNodeType(dto.getNodeType());
        }

        // Handle tags
        if (dto.getTags() != null) {
            List<String> tagNames = dto.getTags().stream()
                    .map(TagDto::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.toList());

            List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
            Map<String, Tag> tagMap = existingTags.stream()
                    .collect(Collectors.toMap(Tag::getName, tag -> tag));

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

        // Handle testcases update (replace all)
        if (dto.getDefaultTestcases() != null || dto.getHiddenTestcases() != null) {
            List<TestCase> newTestCases = new ArrayList<>();

            if (dto.getDefaultTestcases() != null) {
                for (TestCaseDto tc : dto.getDefaultTestcases()) {
                    newTestCases.add(TestCase.builder()
                            .input(tc.getInput())
                            .type(TestCaseType.DEFAULT)
                            .question(question)
                            .build());
                }
            }

            if (dto.getHiddenTestcases() != null) {
                for (TestCaseDto tc : dto.getHiddenTestcases()) {
                    newTestCases.add(TestCase.builder()
                            .input(tc.getInput())
                            .type(TestCaseType.HIDDEN)
                            .question(question)
                            .build());
                }
            }

            question.getTestCases().clear();
            question.getTestCases().addAll(newTestCases);
        }

        // Handle reference solution update
        if (dto.getReferenceSolution() != null) {
            Optional<ReferenceSolution> existingRef = referenceSolutionRepository.findByQuestionId(id);
            ReferenceSolution refSol;
            if (existingRef.isPresent()) {
                refSol = existingRef.get();
                refSol.setLanguage(dto.getReferenceSolution().getLanguage());
                refSol.setSourceCode(dto.getReferenceSolution().getSourceCode());
            } else {
                refSol = ReferenceSolution.builder()
                        .question(question)
                        .language(dto.getReferenceSolution().getLanguage())
                        .sourceCode(dto.getReferenceSolution().getSourceCode())
                        .build();
            }
            referenceSolutionRepository.save(refSol);
        }

        questionsRepository.save(question);

        return getQuestionById(id);
    }

    /**
     * Bulk save questions - each question is processed independently.
     * Failures on individual questions don't affect others.
     */
    public BulkCreateQuestionsResponseDto bulkSaveQuestions(List<QuestionRequestDto> questions) {
        List<BulkCreateQuestionsResponseDto.BulkQuestionResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            QuestionRequestDto dto = questions.get(i);
            String title = dto.getQuestionTitle() != null ? dto.getQuestionTitle() : "Question at index " + i;

            try {
                ResponseEntity<CreateQuestionResponseDto> response = saveQuestion(dto);
                CreateQuestionResponseDto body = response.getBody();

                if (response.getStatusCode().is2xxSuccessful() && body != null && body.getQuestionId() != null) {
                    results.add(BulkCreateQuestionsResponseDto.BulkQuestionResult.builder()
                            .index(i)
                            .questionTitle(title)
                            .questionId(body.getQuestionId())
                            .success(true)
                            .message("Created successfully")
                            .build());
                    successCount++;
                } else {
                    results.add(BulkCreateQuestionsResponseDto.BulkQuestionResult.builder()
                            .index(i)
                            .questionTitle(title)
                            .questionId(null)
                            .success(false)
                            .message(body != null ? body.getMessage() : "Unknown error")
                            .build());
                    failedCount++;
                }
            } catch (Exception e) {
                results.add(BulkCreateQuestionsResponseDto.BulkQuestionResult.builder()
                        .index(i)
                        .questionTitle(title)
                        .questionId(null)
                        .success(false)
                        .message("Exception: " + e.getMessage())
                        .build());
                failedCount++;
            }
        }

        return BulkCreateQuestionsResponseDto.builder()
                .totalReceived(questions.size())
                .successCount(successCount)
                .failedCount(failedCount)
                .results(results)
                .build();
    }
}
