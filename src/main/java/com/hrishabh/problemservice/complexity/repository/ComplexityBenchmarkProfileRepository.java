package com.hrishabh.problemservice.complexity.repository;

import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComplexityBenchmarkProfileRepository extends JpaRepository<ComplexityBenchmarkProfile, Long> {

    Optional<ComplexityBenchmarkProfile> findByQuestionIdAndLanguageAndActiveSlot(
            Long questionId, String language, Integer activeSlot);

    Optional<ComplexityBenchmarkProfile> findByQuestionIdAndLanguageAndProfileVersion(
            Long questionId, String language, String profileVersion);

    Optional<ComplexityBenchmarkProfile> findByProfileId(String profileId);
}
