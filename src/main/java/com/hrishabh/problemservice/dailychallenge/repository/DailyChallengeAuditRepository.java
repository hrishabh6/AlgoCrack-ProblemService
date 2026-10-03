package com.hrishabh.problemservice.dailychallenge.repository;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyChallengeAuditRepository extends JpaRepository<DailyChallengeAudit, Long> {

    List<DailyChallengeAudit> findByChallengeDateOrderByCreatedAtDesc(LocalDate challengeDate);

    List<DailyChallengeAudit> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
}
