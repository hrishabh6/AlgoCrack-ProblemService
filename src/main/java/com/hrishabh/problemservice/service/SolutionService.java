package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Question;
import com.hrishabh.algocrackentityservice.models.Solution;
import com.hrishabh.problemservice.dto.CreateSolutionRequestDto;
import com.hrishabh.problemservice.dto.SolutionResponseDto;
import com.hrishabh.problemservice.dto.UpdateSolutionRequestDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final QuestionsRepository questionsRepository;

    /**
     * Get all solutions for a question
     */
    public List<SolutionResponseDto> getSolutionsByQuestion(Long questionId) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));

        return question.getSolutions().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Add a new solution to a question
     */
    public SolutionResponseDto addSolution(CreateSolutionRequestDto dto) {
        Question question = questionsRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + dto.getQuestionId()));

        Solution solution = Solution.builder()
                .question(question)
                .code(dto.getCode())
                .language(dto.getLanguage())
                .build();

        Solution saved = solutionRepository.save(solution);
        return mapToDto(saved);
    }

    /**
     * Update solution by ID
     */
    public SolutionResponseDto updateSolution(Long solutionId, UpdateSolutionRequestDto dto) {
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID: " + solutionId));

        if (dto.getCode() != null) {
            solution.setCode(dto.getCode());
        }

        if (dto.getLanguage() != null) {
            solution.setLanguage(dto.getLanguage());
        }

        Solution updated = solutionRepository.save(solution);
        return mapToDto(updated);
    }

    /**
     * Delete solution by ID
     */
    public void deleteSolution(Long solutionId) {
        if (!solutionRepository.existsById(solutionId)) {
            throw new ResourceNotFoundException("Solution not found with ID: " + solutionId);
        }
        solutionRepository.deleteById(solutionId);
    }

    /**
     * Map Solution entity to DTO
     */
    private SolutionResponseDto mapToDto(Solution solution) {
        return SolutionResponseDto.builder()
                .id(solution.getId())
                .code(solution.getCode())
                .language(solution.getLanguage())
                .questionId(solution.getQuestion().getId())
                .build();
    }
}
