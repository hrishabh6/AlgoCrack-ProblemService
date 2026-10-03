package com.hrishabh.problemservice.dailychallenge.repository;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {

    Optional<DailyChallenge> findByChallengeDate(LocalDate challengeDate);

    List<DailyChallenge> findByChallengeDateBetweenAndStatusOrderByChallengeDateAsc(
            LocalDate from, LocalDate to, DailyChallengeStatus status);

    boolean existsByQuestionId(Long questionId);
}
