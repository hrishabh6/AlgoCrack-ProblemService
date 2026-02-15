package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Question;
import com.hrishabh.algocrackentityservice.models.TestCase;
import com.hrishabh.algocrackentityservice.models.TestCaseType;
import com.hrishabh.problemservice.dto.TestCaseRequestDto;
import com.hrishabh.problemservice.dto.TestCaseResponseDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.TestcasesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestcasesService {

    private final QuestionsRepository questionsRepository;
    private final TestcasesRepository testcasesRepository;

    /**
     * Add a new test case to a question
     */
    public void addTestCase(TestCaseRequestDto dto) {
        Question question = questionsRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + dto.getQuestionId()));

        TestCase testCase = TestCase.builder()
                .question(question)
                .input(dto.getInput())
                .type(dto.getType())
                .build();

        testcasesRepository.save(testCase);
    }

    /**
     * Get test case by ID
     */
    public TestCaseResponseDto getTestCase(Long id) {
        TestCase testCase = testcasesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with id " + id));

        return mapToDto(testCase);
    }

    /**
     * Get all test cases for a question
     */
    public List<TestCaseResponseDto> getTestCasesByQuestion(Long questionId) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));

        return question.getTestCases().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get test cases for a question filtered by type
     */
    public List<TestCaseResponseDto> getTestCasesByQuestionAndType(Long questionId, TestCaseType type) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));

        return question.getTestCases().stream()
                .filter(tc -> tc.getType() == type)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update test case by ID
     */
    public TestCaseResponseDto updateTestCase(Long id, TestCaseRequestDto dto) {
        TestCase testCase = testcasesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with id " + id));

        if (dto.getInput() != null) {
            testCase.setInput(dto.getInput());
        }

        if (dto.getType() != null) {
            testCase.setType(dto.getType());
        }

        TestCase updated = testcasesRepository.save(testCase);
        return mapToDto(updated);
    }

    /**
     * Delete test case by ID
     */
    public void deleteTestCase(Long id) {
        if (!testcasesRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestCase not found with id " + id);
        }
        testcasesRepository.deleteById(id);
    }

    /**
     * Map TestCase entity to DTO
     */
    private TestCaseResponseDto mapToDto(TestCase testCase) {
        return TestCaseResponseDto.builder()
                .id(testCase.getId())
                .questionId(testCase.getQuestion().getId())
                .input(testCase.getInput())
                .type(testCase.getType())
                .build();
    }
}
