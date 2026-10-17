package com.hrishabh.problemservice.semantic.repository;

import com.hrishabh.problemservice.semantic.model.SemanticProblemProfile;
import com.hrishabh.problemservice.semantic.model.SemanticProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemanticProblemProfileRepository extends JpaRepository<SemanticProblemProfile, Long> {

    Optional<SemanticProblemProfile> findByQuestionIdAndStatus(Long questionId, SemanticProfileStatus status);
}
