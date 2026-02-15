package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.ReferenceSolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenceSolutionRepository extends JpaRepository<ReferenceSolution, Long> {
    Optional<ReferenceSolution> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
