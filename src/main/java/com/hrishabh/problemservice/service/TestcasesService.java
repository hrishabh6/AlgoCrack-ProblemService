package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Question;
import com.hrishabh.algocrackentityservice.models.QuestionMetadata;
import com.hrishabh.algocrackentityservice.models.TestCase;
import com.hrishabh.problemservice.dto.TestCaseRequestDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.helper.Validation;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.TestcasesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestcasesService {

    private final QuestionsRepository questionsRepository;
    private final TestcasesRepository testcasesRepository;
    private final Validation validation = new Validation();

    public TestcasesService(QuestionsRepository questionsRepository, TestcasesRepository testcasesRepository) {
        this.questionsRepository = questionsRepository;
        this.testcasesRepository = testcasesRepository;
    }

    public void addTestCase(TestCaseRequestDto dto) {
        Question question = questionsRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + dto.getQuestionId()));

        // 🔍 Get latest metadata (optional: or by preferred language)
        if (question.getMetadataList().isEmpty()) {
            throw new IllegalArgumentException("No metadata found for the question.");
        }

        QuestionMetadata metadata = question.getMetadataList().get(0); // or filter by language

        // 🔍 Validate input and expectedOutput against metadata
        validation.validateTestCaseInputAndOutput(dto.getInput(), dto.getExpectedOutput(), metadata);

        // ✅ Save after validation
        TestCase testCase = TestCase.builder()
                .question(question)
                .input(dto.getInput())
                .expectedOutput(dto.getExpectedOutput())
                .orderIndex(dto.getOrderIndex())
                .isHidden(dto.getIsHidden())
                .build();

        testcasesRepository.save(testCase);

        // Later, you want to get test cases for that question
        Question savedQuestion = questionsRepository.findById(question.getId()).get();
        List<TestCase> testCases = savedQuestion.getTestCases(); // ✅ Will give test cases

        for (TestCase tc : testCases) {
            System.out.println("----- Test Case -----");
            System.out.println(tc.getId() + " " + tc.getInput() + " " + tc.getExpectedOutput());
        }

    }


}
