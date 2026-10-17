package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.DailyChallengeContextResponse;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.dailychallenge.repository.PotdProblemMetadataRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyChallengeContextTest {

    @Mock
    private DailyChallengeRepository dailyChallengeRepository;
    @Mock
    private PotdProblemMetadataRepository potdProblemMetadataRepository;

    private DailyChallengeReadService readService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T12:00:00Z"), ZoneOffset.UTC);
        readService = new DailyChallengeReadService(dailyChallengeRepository, potdProblemMetadataRepository, clock);
    }

    @Test
    void validWhenQuestionMatchesPublishedChallenge() {
        LocalDate date = LocalDate.of(2026, 10, 17);
        Question question = Question.builder().questionTitle("Two Sum").build();
        question.setId(12L);
        DailyChallenge challenge = DailyChallenge.builder()
                .challengeDate(date)
                .question(question)
                .status(DailyChallengeStatus.PUBLISHED)
                .build();
        when(dailyChallengeRepository.findByChallengeDate(date)).thenReturn(Optional.of(challenge));

        DailyChallengeContextResponse ctx = readService.resolveContext(date, 12L);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.isActiveToday()).isTrue();
        assertThat(ctx.getExpectedQuestionId()).isEqualTo(12L);
    }

    @Test
    void invalidWhenQuestionDoesNotMatchChallenge() {
        LocalDate date = LocalDate.of(2026, 10, 17);
        Question question = Question.builder().questionTitle("Two Sum").build();
        question.setId(12L);
        DailyChallenge challenge = DailyChallenge.builder()
                .challengeDate(date)
                .question(question)
                .status(DailyChallengeStatus.PUBLISHED)
                .build();
        when(dailyChallengeRepository.findByChallengeDate(date)).thenReturn(Optional.of(challenge));

        DailyChallengeContextResponse ctx = readService.resolveContext(date, 99L);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getExpectedQuestionId()).isEqualTo(12L);
    }

    @Test
    void historicalDatesRemainRecognizable() {
        LocalDate date = LocalDate.of(2026, 10, 16);
        Question question = Question.builder().questionTitle("Old").build();
        question.setId(5L);
        DailyChallenge challenge = DailyChallenge.builder()
                .challengeDate(date)
                .question(question)
                .status(DailyChallengeStatus.PUBLISHED)
                .build();
        when(dailyChallengeRepository.findByChallengeDate(date)).thenReturn(Optional.of(challenge));

        DailyChallengeContextResponse ctx = readService.resolveContext(date, 5L);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.isHistorical()).isTrue();
        assertThat(ctx.isActiveToday()).isFalse();
    }
}
