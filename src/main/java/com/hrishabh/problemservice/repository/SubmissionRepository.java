package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Get user's submissions ordered by most recent.
     */
    Page<Submission> findByUser_UserIdOrderByQueuedAtDesc(String userId, Pageable pageable);

    /**
     * Count unique solved questions by user and difficulty.
     * Status must be COMPLETED and Verdict must be ACCEPTED.
     */
    @Query("SELECT COUNT(DISTINCT s.question.id) FROM Submission s WHERE s.user.userId = :userId AND s.question.difficultyLevel = :difficulty AND s.status = 'COMPLETED' AND s.verdict = 'ACCEPTED'")
    long countDistinctSolvedByUserIdAndDifficulty(@Param("userId") String userId, @Param("difficulty") String difficulty);

    /**
     * Count unique solved questions by user and language.
     * Status must be COMPLETED and Verdict must be ACCEPTED.
     * Returns list of Object[] {language, count}
     */
    @Query("SELECT s.language, COUNT(DISTINCT s.question.id) FROM Submission s WHERE s.user.userId = :userId AND s.status = 'COMPLETED' AND s.verdict = 'ACCEPTED' GROUP BY s.language")
    List<Object[]> countDistinctSolvedByUserIdGroupByLanguage(@Param("userId") String userId);

    /**
     * Get submission counts grouped by date for heatmap.
     * Returns list of Object[] { date (LocalDate), count (Long) }
     * for any day where the user made at least 1 submission within the given range.
     */
    @Query("SELECT FUNCTION('DATE', s.queuedAt), COUNT(s) " +
           "FROM Submission s " +
           "WHERE s.user.userId = :userId " +
           "AND s.queuedAt >= :from " +
           "AND s.queuedAt < :to " +
           "GROUP BY FUNCTION('DATE', s.queuedAt) " +
           "ORDER BY FUNCTION('DATE', s.queuedAt) ASC")
    List<Object[]> countSubmissionsGroupedByDateBetween(
            @Param("userId") String userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
