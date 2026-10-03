package com.hrishabh.problemservice.dailychallenge.repository;

import com.hrishabh.problemservice.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PotdEligibleQuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            SELECT DISTINCT q FROM Question q
            INNER JOIN PotdProblemMetadata m ON m.question.id = q.id
            LEFT JOIN FETCH q.tags
            WHERE q.status = com.hrishabh.problemservice.models.QuestionStatus.PUBLISHED
              AND m.eligible = true
              AND m.curationStatus = com.hrishabh.problemservice.dailychallenge.model.PotdCurationStatus.APPROVED
              AND m.validationStatus = com.hrishabh.problemservice.dailychallenge.model.PotdValidationStatus.VALID
            ORDER BY q.id ASC
            """)
    List<Question> findCatalogEligibleForPotd();
}
