package com.hrishabh.problemservice.dailychallenge.repository;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {

    @Query("""
            SELECT dc FROM DailyChallenge dc
            JOIN FETCH dc.question q
            LEFT JOIN FETCH q.tags
            WHERE dc.challengeDate = :challengeDate
            """)
    Optional<DailyChallenge> findByChallengeDate(@Param("challengeDate") LocalDate challengeDate);

    @Query("""
            SELECT dc FROM DailyChallenge dc
            JOIN FETCH dc.question q
            LEFT JOIN FETCH q.tags
            WHERE dc.challengeDate BETWEEN :from AND :to AND dc.status = :status
            ORDER BY dc.challengeDate ASC
            """)
    List<DailyChallenge> findByChallengeDateBetweenAndStatusOrderByChallengeDateAsc(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") DailyChallengeStatus status);

    boolean existsByQuestionId(Long questionId);

    @Query("""
            SELECT dc FROM DailyChallenge dc
            JOIN FETCH dc.question q
            LEFT JOIN FETCH q.tags
            WHERE dc.challengeDate BETWEEN :from AND :to
            AND (:status IS NULL OR dc.status = :status)
            ORDER BY dc.challengeDate ASC
            """)
    List<DailyChallenge> findAdminRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") DailyChallengeStatus status);

    @Query("""
            SELECT dc FROM DailyChallenge dc
            JOIN FETCH dc.question
            WHERE dc.challengeDate BETWEEN :from AND :to
            ORDER BY dc.challengeDate ASC
            """)
    List<DailyChallenge> findByChallengeDateBetweenOrderByChallengeDateAsc(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT dc FROM DailyChallenge dc
            JOIN FETCH dc.question
            WHERE dc.challengeDate <= :to AND dc.status = :status
            ORDER BY dc.challengeDate ASC
            """)
    List<DailyChallenge> findPublishedThrough(
            @Param("to") LocalDate to,
            @Param("status") DailyChallengeStatus status);
}
