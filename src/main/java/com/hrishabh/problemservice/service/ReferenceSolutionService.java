package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.ReferenceSolution;
import com.hrishabh.problemservice.dto.ReferenceSolutionDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.ReferenceSolutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferenceSolutionService {

    private final ReferenceSolutionRepository referenceSolutionRepository;
    private final QuestionsRepository questionsRepository;

    /**
     * Get reference solution by question ID
     */
    public ReferenceSolutionDto getByQuestionId(Long questionId) {
        ReferenceSolution refSol = referenceSolutionRepository.findByQuestionId(questionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reference solution not found for question " + questionId));

        return mapToDto(refSol);
    }

    /**
     * Check if a reference solution exists for a question
     */
    public boolean existsForQuestion(Long questionId) {
        return referenceSolutionRepository.findByQuestionId(questionId).isPresent();
    }

    /**
     * Create or update reference solution for a question
     */
    @Transactional
    public ReferenceSolutionDto createOrUpdate(Long questionId, ReferenceSolutionDto dto) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));

        Optional<ReferenceSolution> existing = referenceSolutionRepository.findByQuestionId(questionId);

        ReferenceSolution refSol;
        if (existing.isPresent()) {
            refSol = existing.get();
            refSol.setLanguage(dto.getLanguage());
            refSol.setSourceCode(dto.getSourceCode());
        } else {
            refSol = ReferenceSolution.builder()
                    .question(question)
                    .language(dto.getLanguage())
                    .sourceCode(dto.getSourceCode())
                    .build();
        }

        ReferenceSolution saved = referenceSolutionRepository.save(refSol);
        return mapToDto(saved);
    }

    /**
     * Delete reference solution for a question
     */
    @Transactional
    public void delete(Long questionId) {
        if (!referenceSolutionRepository.findByQuestionId(questionId).isPresent()) {
            throw new ResourceNotFoundException("Reference solution not found for question " + questionId);
        }
        referenceSolutionRepository.deleteByQuestionId(questionId);
    }

    /**
     * Map ReferenceSolution entity to DTO
     */
    private ReferenceSolutionDto mapToDto(ReferenceSolution refSol) {
        return ReferenceSolutionDto.builder()
                .language(refSol.getLanguage())
                .sourceCode(refSol.getSourceCode())
                .build();
    }
}
