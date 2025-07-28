package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Solution;
import com.hrishabh.problemservice.dto.SolutionResponseDto;
import com.hrishabh.problemservice.dto.UpdateSolutionRequestDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolutionService {

    private final SolutionRepository solutionRepository;

    public SolutionService(SolutionRepository solutionRepository) {
        this.solutionRepository = solutionRepository;
    }

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

        return SolutionResponseDto.builder()
                .id(updated.getId())
                .code(updated.getCode())
                .language(updated.getLanguage())
                .questionId(updated.getQuestion().getId())
                .build();
    }


}
