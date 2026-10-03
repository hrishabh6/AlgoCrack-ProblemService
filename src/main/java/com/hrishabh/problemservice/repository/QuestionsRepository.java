package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionsRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @Query("""
            SELECT q FROM Question q
            LEFT JOIN FETCH q.testCases
            LEFT JOIN FETCH q.referenceSolution
            LEFT JOIN FETCH q.tags
            WHERE q.id = :id
            """)
    Optional<Question> findByIdWithJudgingAssets(@Param("id") long id);

    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Question findRandomQuestion();

    long countByDifficultyLevel(String difficultyLevel);

    @Query("""
            SELECT DISTINCT q FROM Question q
            LEFT JOIN FETCH q.tags
            WHERE q.id IN :ids
            """)
    List<Question> findByIdInWithTags(@Param("ids") Collection<Long> ids);
}
