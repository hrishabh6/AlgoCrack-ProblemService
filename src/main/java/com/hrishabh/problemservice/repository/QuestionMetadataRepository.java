package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.QuestionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionMetadataRepository extends JpaRepository<QuestionMetadata, Long> {
}
