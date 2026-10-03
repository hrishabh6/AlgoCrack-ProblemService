package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengeAdminDtos.AuditEntryDto;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengeAdminDtos.AuditListResponse;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeAudit;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeAuditAction;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyChallengeAuditService {

    private final DailyChallengeAuditRepository auditRepository;

    public DailyChallengeAuditService(DailyChallengeAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public void record(
            Long challengeId,
            LocalDate challengeDate,
            DailyChallengeAuditAction action,
            Long oldQuestionId,
            Long newQuestionId,
            String actor,
            String reason,
            String requestId) {
        auditRepository.save(DailyChallengeAudit.builder()
                .challengeId(challengeId)
                .challengeDate(challengeDate)
                .action(action)
                .oldQuestionId(oldQuestionId)
                .newQuestionId(newQuestionId)
                .actor(actor)
                .reason(reason)
                .requestId(requestId)
                .build());
    }

    @Transactional(readOnly = true)
    public AuditListResponse listForDate(LocalDate date) {
        List<AuditEntryDto> entries = auditRepository.findByChallengeDateOrderByCreatedAtDesc(date).stream()
                .map(a -> AuditEntryDto.builder()
                        .id(a.getId())
                        .challengeDate(a.getChallengeDate())
                        .action(a.getAction().name())
                        .oldQuestionId(a.getOldQuestionId())
                        .newQuestionId(a.getNewQuestionId())
                        .actor(a.getActor())
                        .reason(a.getReason())
                        .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null)
                        .build())
                .toList();
        return AuditListResponse.builder().entries(entries).build();
    }
}
