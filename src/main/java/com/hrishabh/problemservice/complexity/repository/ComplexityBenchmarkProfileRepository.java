package com.hrishabh.problemservice.complexity.repository;

import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.model.ComplexityProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplexityBenchmarkProfileRepository extends JpaRepository<ComplexityBenchmarkProfile, Long> {

    Optional<ComplexityBenchmarkProfile> findFirstByQuestionIdAndLanguageAndStatusOrderByProfileVersionDesc(
            Long questionId, String language, ComplexityProfileStatus status);

    Optional<ComplexityBenchmarkProfile> findByQuestionIdAndLanguageAndProfileVersion(
            Long questionId, String language, String profileVersion);

    Optional<ComplexityBenchmarkProfile> findByProfileId(String profileId);
}
