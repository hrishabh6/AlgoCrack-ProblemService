package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.Language;
import com.hrishabh.problemservice.models.QuestionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionMetadataRepository extends JpaRepository<QuestionMetadata, Long> {
    Optional<QuestionMetadata> findByQuestionIdAndLanguage(Long questionId, Language language);
}
