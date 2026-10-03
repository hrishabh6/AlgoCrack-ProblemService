package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.DailyChallengeRangeResponse;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.DailyChallengeResponse;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.ProblemSummary;
import com.hrishabh.problemservice.dailychallenge.exception.PotdNotScheduledException;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallenge;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import com.hrishabh.problemservice.dailychallenge.model.PotdProblemMetadata;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.dailychallenge.repository.PotdProblemMetadataRepository;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DailyChallengeReadService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final PotdProblemMetadataRepository potdProblemMetadataRepository;
    private final Clock clock;

    public DailyChallengeReadService(
            DailyChallengeRepository dailyChallengeRepository,
            PotdProblemMetadataRepository potdProblemMetadataRepository,
            Clock clock) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.potdProblemMetadataRepository = potdProblemMetadataRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DailyChallengeResponse getToday() {
        LocalDate today = LocalDate.now(clock);
        return getPublishedForDate(today).orElseThrow(PotdNotScheduledException::new);
    }

    @Transactional(readOnly = true)
    public DailyChallengeResponse getByDate(LocalDate date) {
        validateDate(date);
        return getPublishedForDate(date).orElseThrow(PotdNotScheduledException::new);
    }

    @Transactional(readOnly = true)
    public DailyChallengeRangeResponse getRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate cappedTo = to.isAfter(today) ? today : to;
        if (from.isAfter(cappedTo)) {
            return DailyChallengeRangeResponse.builder().challenges(List.of()).build();
        }
        List<DailyChallenge> rows = dailyChallengeRepository.findByChallengeDateBetweenAndStatusOrderByChallengeDateAsc(
                from, cappedTo, DailyChallengeStatus.PUBLISHED);
        List<DailyChallengeResponse> mapped = new ArrayList<>();
        for (DailyChallenge row : rows) {
            mapped.add(toResponse(row));
        }
        return DailyChallengeRangeResponse.builder().challenges(mapped).build();
    }

    private Optional<DailyChallengeResponse> getPublishedForDate(LocalDate date) {
        validateDate(date);
        LocalDate today = LocalDate.now(clock);
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        return dailyChallengeRepository.findByChallengeDate(date)
                .filter(dc -> dc.getStatus() == DailyChallengeStatus.PUBLISHED)
                .map(this::toResponse);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }
    }

    private DailyChallengeResponse toResponse(DailyChallenge challenge) {
        Question question = challenge.getQuestion();
        String primaryTopic = resolvePrimaryTopic(question.getId());
        List<String> tagNames = question.getTags().stream().map(Tag::getName).toList();
        Instant startsAt = challenge.getChallengeDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant nextResetAt = challenge.getChallengeDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return DailyChallengeResponse.builder()
                .id(challenge.getId())
                .challengeDate(challenge.getChallengeDate())
                .startsAt(startsAt)
                .nextResetAt(nextResetAt)
                .problem(ProblemSummary.builder()
                        .id(question.getId())
                        .title(question.getQuestionTitle())
                        .difficulty(question.getDifficultyLevel())
                        .primaryTopic(primaryTopic)
                        .tags(tagNames)
                        .href("/problems/" + question.getId())
                        .build())
                .build();
    }

    private String resolvePrimaryTopic(long questionId) {
        Optional<PotdProblemMetadata> meta = potdProblemMetadataRepository.findById(questionId);
        if (meta.isEmpty() || meta.get().getPrimaryTag() == null) {
            return null;
        }
        return meta.get().getPrimaryTag().getName();
    }
}
