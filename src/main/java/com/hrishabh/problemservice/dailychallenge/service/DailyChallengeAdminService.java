package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.config.PotdSchedulerProperties;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengeAdminDtos.*;
import com.hrishabh.problemservice.dailychallenge.exception.PotdUnprocessableException;
import com.hrishabh.problemservice.dailychallenge.model.*;
import com.hrishabh.problemservice.dailychallenge.planner.PotdConfigurationHasher;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.FixedAssignment;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannedAssignment;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannerRequest;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PlannerResult;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PotdPlanningCandidate;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.dailychallenge.repository.PotdEligibleQuestionRepository;
import com.hrishabh.problemservice.exceptions.ConflictException;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DailyChallengeAdminService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final DailyChallengePlanner planner;
    private final PotdCandidateLoader candidateLoader;
    private final PotdEligibilityService eligibilityService;
    private final DailyChallengeAuditService auditService;
    private final PotdSchedulerProperties properties;
    private final PotdEligibleQuestionRepository eligibleQuestionRepository;
    private final QuestionsRepository questionsRepository;
    private final Clock clock;

    public DailyChallengeAdminService(
            DailyChallengeRepository dailyChallengeRepository,
            DailyChallengePlanner planner,
            PotdCandidateLoader candidateLoader,
            PotdEligibilityService eligibilityService,
            DailyChallengeAuditService auditService,
            PotdSchedulerProperties properties,
            PotdEligibleQuestionRepository eligibleQuestionRepository,
            QuestionsRepository questionsRepository,
            Clock clock) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.planner = planner;
        this.candidateLoader = candidateLoader;
        this.eligibilityService = eligibilityService;
        this.auditService = auditService;
        this.properties = properties;
        this.eligibleQuestionRepository = eligibleQuestionRepository;
        this.questionsRepository = questionsRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminChallengeListResponse list(LocalDate from, LocalDate to, DailyChallengeStatus status) {
        validateRange(from, to);
        List<AdminChallengeRow> rows = dailyChallengeRepository.findAdminRange(from, to, status).stream()
                .map(this::toAdminRow)
                .toList();
        return AdminChallengeListResponse.builder().challenges(rows).build();
    }

    @Transactional
    public GenerationResponse generate(GenerationRequest request, String actor) {
        LocalDate today = LocalDate.now(clock);
        assertFutureRange(request.getFrom(), request.getTo(), today);
        List<PotdPlanningCandidate> candidates = candidateLoader.loadEligibleCandidates();
        if (candidates.isEmpty()) {
            throw new PotdUnprocessableException("No eligible POTD candidates in catalogue");
        }

        Map<LocalDate, DailyChallenge> existing = indexByDate(dailyChallengeRepository.findByChallengeDateBetweenOrderByChallengeDateAsc(
                request.getFrom(), request.getTo()));
        List<LocalDate> datesToFill = new ArrayList<>();
        List<FixedAssignment> fixed = new ArrayList<>();
        collectPlanningSlots(request, existing, datesToFill, fixed);

        LocalDate historyStart = request.getFrom().minusDays(properties.getHistoryWindowDays());
        Map<LocalDate, Long> history = loadHistory(historyStart, request.getFrom().minusDays(1));

        PlannerResult planned = planner.plan(new PlannerRequest(
                request.getFrom(),
                request.getTo(),
                datesToFill,
                candidates,
                fixed,
                history));

        List<PlannedRowDto> plannedDtos = planned.assignments().stream()
                .map(p -> PlannedRowDto.builder()
                        .challengeDate(p.challengeDate())
                        .questionId(p.questionId())
                        .selectionScore(p.selectionScore())
                        .build())
                .toList();

        if (request.isDryRun()) {
            return GenerationResponse.builder()
                    .dryRun(true)
                    .configurationHash(planned.configurationHash())
                    .planned(plannedDtos)
                    .persistedCount(0)
                    .build();
        }

        int persisted = 0;
        String configHash = planned.configurationHash();
        for (PlannedAssignment assignment : planned.assignments()) {
            persistGeneratedRow(existing, assignment, configHash, actor);
            persisted++;
        }
        return GenerationResponse.builder()
                .dryRun(false)
                .configurationHash(configHash)
                .planned(plannedDtos)
                .persistedCount(persisted)
                .build();
    }

    @Transactional
    public PublicationResponse publish(PublicationRequest request, String actor) {
        validateRange(request.getFrom(), request.getTo());
        List<DailyChallenge> drafts = dailyChallengeRepository.findByChallengeDateBetweenAndStatusOrderByChallengeDateAsc(
                request.getFrom(), request.getTo(), DailyChallengeStatus.DRAFT);
        int count = 0;
        Date now = new Date();
        for (DailyChallenge draft : drafts) {
            if (draft.isLocked()) {
                continue;
            }
            eligibilityService.requireEligibleQuestion(draft.getQuestion().getId());
            draft.setStatus(DailyChallengeStatus.PUBLISHED);
            draft.setPublishedAt(now);
            draft.setPublishedBy(actor);
            draft.setUpdatedBy(actor);
            auditService.record(
                    draft.getId(),
                    draft.getChallengeDate(),
                    DailyChallengeAuditAction.PUBLISHED,
                    null,
                    draft.getQuestion().getId(),
                    actor,
                    request.getReason(),
                    null);
            count++;
        }
        return PublicationResponse.builder().publishedCount(count).build();
    }

    @Transactional
    public AdminChallengeRow manualReplace(LocalDate date, ManualReplaceRequest request, String actor) {
        assertFutureOrToday(date);
        Question question = eligibilityService.requireEligibleQuestion(request.getQuestionId());
        Optional<DailyChallenge> existing = dailyChallengeRepository.findByChallengeDate(date);
        if (existing.isPresent()) {
            DailyChallenge row = existing.get();
            if (request.getExpectedVersion() != null && row.getVersion() != request.getExpectedVersion()) {
                throw new ConflictException("Stale challenge version");
            }
            if (row.isLocked()) {
                throw new ConflictException("Challenge date is locked");
            }
            if (row.getStatus() == DailyChallengeStatus.PUBLISHED) {
                throw new ConflictException("Published row requires emergency workflow; cancel first");
            }
            Long oldQ = row.getQuestion().getId();
            applyManualSelection(row, question, actor, request.getReason());
            auditService.record(
                    row.getId(),
                    date,
                    DailyChallengeAuditAction.REPLACED,
                    oldQ,
                    question.getId(),
                    actor,
                    request.getReason(),
                    null);
            return toAdminRow(row);
        }

        DailyChallenge created = DailyChallenge.builder()
                .challengeDate(date)
                .question(question)
                .status(DailyChallengeStatus.DRAFT)
                .selectionType(DailyChallengeSelectionType.MANUAL)
                .locked(true)
                .schedulerVersion(properties.getSchedulerVersion())
                .configurationHash(PotdConfigurationHasher.hash(properties))
                .deterministicSeed("manual-" + date + "-" + question.getId())
                .createdBy(actor)
                .updatedBy(actor)
                .build();
        dailyChallengeRepository.save(created);
        auditService.record(
                created.getId(),
                date,
                DailyChallengeAuditAction.REPLACED,
                null,
                question.getId(),
                actor,
                request.getReason(),
                null);
        return toAdminRow(created);
    }

    @Transactional
    public AdminChallengeRow lock(LocalDate date, ReasonRequest request, String actor) {
        DailyChallenge row = requireRow(date);
        if (row.isLocked()) {
            throw new ConflictException("Already locked");
        }
        row.setLocked(true);
        row.setUpdatedBy(actor);
        auditService.record(
                row.getId(), date, DailyChallengeAuditAction.LOCKED, null, null, actor, request.getReason(), null);
        return toAdminRow(row);
    }

    @Transactional
    public AdminChallengeRow unlock(LocalDate date, ReasonRequest request, String actor) {
        DailyChallenge row = requireRow(date);
        if (!row.isLocked()) {
            throw new ConflictException("Not locked");
        }
        row.setLocked(false);
        row.setUpdatedBy(actor);
        auditService.record(
                row.getId(), date, DailyChallengeAuditAction.UNLOCKED, null, null, actor, request.getReason(), null);
        return toAdminRow(row);
    }

    @Transactional
    public AdminChallengeRow cancel(LocalDate date, ReasonRequest request, String actor) {
        DailyChallenge row = requireRow(date);
        if (row.getStatus() == DailyChallengeStatus.CANCELLED) {
            throw new ConflictException("Already cancelled");
        }
        Long qid = row.getQuestion().getId();
        row.setStatus(DailyChallengeStatus.CANCELLED);
        row.setUpdatedBy(actor);
        auditService.record(
                row.getId(),
                date,
                DailyChallengeAuditAction.CANCELLED,
                qid,
                null,
                actor,
                request.getReason(),
                null);
        return toAdminRow(row);
    }

    @Transactional(readOnly = true)
    public PreflightResponse preflight() {
        List<Question> eligible = eligibleQuestionRepository.findCatalogEligibleForPotd().stream()
                .filter(eligibilityService::isStructurallyComplete)
                .toList();
        int easy = 0;
        int medium = 0;
        int hard = 0;
        for (Question q : eligible) {
            String d = q.getDifficultyLevel() == null ? "" : q.getDifficultyLevel().trim().toLowerCase();
            switch (d) {
                case "easy" -> easy++;
                case "medium" -> medium++;
                case "hard" -> hard++;
                default -> {
                }
            }
        }
        int count = eligible.size();
        int horizon = properties.getDraftHorizonDays();
        int cooldown = properties.getExactCooldownDays();
        boolean sufficient = count > 0 && count >= Math.min(horizon, cooldown);
        String note = sufficient
                ? "Catalogue has eligible candidates for a dev horizon"
                : "Curate and approve more POTD-eligible questions before scheduling";
        return PreflightResponse.builder()
                .eligibleQuestionCount(count)
                .easyCount(easy)
                .mediumCount(medium)
                .hardCount(hard)
                .draftHorizonDays(horizon)
                .exactCooldownDays(cooldown)
                .sufficientForHorizon(sufficient)
                .note(note)
                .build();
    }

    private void persistGeneratedRow(
            Map<LocalDate, DailyChallenge> existing,
            PlannedAssignment assignment,
            String configHash,
            String actor) {
        DailyChallenge row = existing.get(assignment.challengeDate());
        DailyChallengeAuditAction action = DailyChallengeAuditAction.GENERATED;
        if (row != null) {
            if (row.getStatus() == DailyChallengeStatus.PUBLISHED || row.isLocked()) {
                throw new ConflictException("Cannot overwrite published or locked row for " + assignment.challengeDate());
            }
            action = DailyChallengeAuditAction.REGENERATED;
            row.setQuestion(loadQuestionRef(assignment.questionId()));
            row.setSelectionScore(assignment.selectionScore());
            row.setDeterministicSeed(assignment.deterministicSeed());
            row.setConfigurationHash(configHash);
            row.setSelectionType(DailyChallengeSelectionType.AUTO);
            row.setUpdatedBy(actor);
        } else {
            row = DailyChallenge.builder()
                    .challengeDate(assignment.challengeDate())
                    .question(loadQuestionRef(assignment.questionId()))
                    .status(DailyChallengeStatus.DRAFT)
                    .selectionType(DailyChallengeSelectionType.AUTO)
                    .schedulerVersion(properties.getSchedulerVersion())
                    .configurationHash(configHash)
                    .deterministicSeed(assignment.deterministicSeed())
                    .selectionScore(assignment.selectionScore())
                    .createdBy(actor)
                    .updatedBy(actor)
                    .build();
            dailyChallengeRepository.save(row);
        }
        auditService.record(
                row.getId(),
                assignment.challengeDate(),
                action,
                null,
                assignment.questionId(),
                actor,
                "auto-generation",
                null);
    }

    private Question loadQuestionRef(long questionId) {
        return questionsRepository.getReferenceById(questionId);
    }

    private void applyManualSelection(DailyChallenge row, Question question, String actor, String reason) {
        row.setQuestion(question);
        row.setSelectionType(DailyChallengeSelectionType.MANUAL);
        row.setSelectionScore(null);
        row.setDeterministicSeed("manual-" + row.getChallengeDate() + "-" + question.getId());
        row.setUpdatedBy(actor);
        row.setLocked(true);
    }

    private DailyChallenge requireRow(LocalDate date) {
        return dailyChallengeRepository
                .findByChallengeDate(date)
                .orElseThrow(() -> new PotdUnprocessableException("No challenge for date " + date));
    }

    private AdminChallengeRow toAdminRow(DailyChallenge dc) {
        return AdminChallengeRow.builder()
                .id(dc.getId())
                .challengeDate(dc.getChallengeDate())
                .status(dc.getStatus())
                .selectionType(dc.getSelectionType())
                .locked(dc.isLocked())
                .questionId(dc.getQuestion().getId())
                .questionTitle(dc.getQuestion().getQuestionTitle())
                .selectionScore(dc.getSelectionScore())
                .schedulerVersion(dc.getSchedulerVersion())
                .configurationHash(dc.getConfigurationHash())
                .version(dc.getVersion())
                .build();
    }

    private static Map<LocalDate, DailyChallenge> indexByDate(List<DailyChallenge> rows) {
        Map<LocalDate, DailyChallenge> map = new HashMap<>();
        for (DailyChallenge row : rows) {
            map.put(row.getChallengeDate(), row);
        }
        return map;
    }

    private static void collectPlanningSlots(
            GenerationRequest request,
            Map<LocalDate, DailyChallenge> existing,
            List<LocalDate> datesToFill,
            List<FixedAssignment> fixed) {
        for (LocalDate d = request.getFrom(); !d.isAfter(request.getTo()); d = d.plusDays(1)) {
            DailyChallenge row = existing.get(d);
            if (row == null) {
                datesToFill.add(d);
            } else if (row.getStatus() == DailyChallengeStatus.PUBLISHED || row.isLocked()) {
                fixed.add(new FixedAssignment(d, row.getQuestion().getId(), row.isLocked()));
            } else if (row.getStatus() == DailyChallengeStatus.DRAFT && request.isReplaceUnlockedDrafts()) {
                datesToFill.add(d);
            } else if (row.getStatus() == DailyChallengeStatus.CANCELLED) {
                datesToFill.add(d);
            }
        }
    }

    private Map<LocalDate, Long> loadHistory(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return Map.of();
        }
        Map<LocalDate, Long> history = new HashMap<>();
        for (DailyChallenge dc : dailyChallengeRepository.findByChallengeDateBetweenOrderByChallengeDateAsc(from, to)) {
            history.put(dc.getChallengeDate(), dc.getQuestion().getId());
        }
        return history;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }
    }

    private void assertFutureRange(LocalDate from, LocalDate to, LocalDate today) {
        validateRange(from, to);
        if (!from.isAfter(today)) {
            throw new IllegalArgumentException("Generation range must start after UTC today");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > properties.getMaxPlanningHorizonDays()) {
            throw new IllegalArgumentException("Range exceeds max planning horizon");
        }
    }

    private void assertFutureOrToday(LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Date must not be in the past");
        }
    }
}
