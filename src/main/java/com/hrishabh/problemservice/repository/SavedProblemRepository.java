package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.SavedProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedProblemRepository extends JpaRepository<SavedProblem, Long> {

    boolean existsByUserIdAndQuestionId(String userId, Long questionId);

    @Query("SELECT s.questionId FROM SavedProblem s WHERE s.userId = :userId ORDER BY s.createdAt DESC, s.id DESC")
    List<Long> findQuestionIdsByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM SavedProblem s WHERE s.userId = :userId AND s.questionId = :questionId")
    int deleteByUserIdAndQuestionId(@Param("userId") String userId, @Param("questionId") Long questionId);
}
