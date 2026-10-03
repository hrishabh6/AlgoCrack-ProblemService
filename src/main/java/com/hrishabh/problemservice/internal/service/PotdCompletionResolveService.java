package com.hrishabh.problemservice.internal.service;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.internal.RankDifficultyNormalizer;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.PotdResolveResponse;
import com.hrishabh.problemservice.models.Question;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class PotdCompletionResolveService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final Clock clock;

    public PotdCompletionResolveService(DailyChallengeRepository dailyChallengeRepository, Clock clock) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PotdResolveResponse resolve(long questionId, Instant completedAt) {
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt is required");
        }
        LocalDate challengeDate = completedAt.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate todayUtc = LocalDate.now(clock);

        if (challengeDate.isAfter(todayUtc)) {
            return PotdResolveResponse.builder()
                    .matched(false)
                    .qualifies(false)
                    .questionId(questionId)
                    .build();
        }

        return dailyChallengeRepository.findPublishedForDateAndQuestion(
                        challengeDate, questionId, DailyChallengeStatus.PUBLISHED)
                .map(dc -> toResponse(dc, questionId, completedAt, challengeDate))
                .orElseGet(() -> PotdResolveResponse.builder()
                        .matched(false)
                        .qualifies(false)
                        .questionId(questionId)
                        .challengeDate(challengeDate.toString())
                        .build());
    }

    private static PotdResolveResponse toResponse(
            DailyChallenge dc,
            long questionId,
            Instant completedAt,
            LocalDate challengeDate) {
        Question question = dc.getQuestion();
        long resolvedQuestionId = question != null ? question.getId() : questionId;
        Instant windowStart = challengeDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant windowEnd = challengeDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        boolean qualifies = !completedAt.isBefore(windowStart) && completedAt.isBefore(windowEnd);
        String rawDifficulty = question != null ? question.getDifficultyLevel() : null;

        return PotdResolveResponse.builder()
                .matched(true)
                .qualifies(qualifies)
                .challengeId(dc.getId())
                .challengeDate(challengeDate.toString())
                .questionId(resolvedQuestionId)
                .challengeStatus(dc.getStatus().name())
                .difficultyLevel(rawDifficulty)
                .normalizedDifficulty(RankDifficultyNormalizer.normalize(rawDifficulty))
                .build();
    }
}
