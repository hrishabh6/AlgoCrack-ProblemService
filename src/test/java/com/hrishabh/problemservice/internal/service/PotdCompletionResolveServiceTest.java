package com.hrishabh.problemservice.internal.service;

import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.PotdResolveResponse;
import com.hrishabh.problemservice.models.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PotdCompletionResolveServiceTest {

    @Mock
    private DailyChallengeRepository dailyChallengeRepository;

    private PotdCompletionResolveService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-03T12:00:00Z"), ZoneOffset.UTC);
        service = new PotdCompletionResolveService(dailyChallengeRepository, clock);
    }

    @Test
    void qualifiesWhenCompletedWithinUtcChallengeDay() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        Question question = Question.builder().difficultyLevel("Medium").build();
        question.setId(42L);
        DailyChallenge challenge = DailyChallenge.builder()
                .challengeDate(date)
                .question(question)
                .status(DailyChallengeStatus.PUBLISHED)
                .build();
        challenge.setId(7L);
        when(dailyChallengeRepository.findPublishedForDateAndQuestion(
                eq(date), eq(42L), eq(DailyChallengeStatus.PUBLISHED)))
                .thenReturn(Optional.of(challenge));

        Instant completedAt = Instant.parse("2026-10-02T23:59:00Z");
        PotdResolveResponse response = service.resolve(42L, completedAt);

        assertThat(response.isMatched()).isTrue();
        assertThat(response.isQualifies()).isTrue();
        assertThat(response.getChallengeId()).isEqualTo(7L);
        assertThat(response.getNormalizedDifficulty()).isEqualTo("MEDIUM");
    }

    @Test
    void doesNotExposeFutureChallengeDates() {
        PotdResolveResponse response = service.resolve(42L, Instant.parse("2026-10-04T12:00:00Z"));

        assertThat(response.isMatched()).isFalse();
        assertThat(response.isQualifies()).isFalse();
    }
}
