package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.dto.PotdMetadataAdminDtos.MetadataResponse;
import com.hrishabh.problemservice.dailychallenge.dto.PotdMetadataAdminDtos.UpsertMetadataRequest;
import com.hrishabh.problemservice.dailychallenge.model.PotdProblemMetadata;
import com.hrishabh.problemservice.dailychallenge.repository.PotdProblemMetadataRepository;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PotdMetadataAdminService {

    private final PotdProblemMetadataRepository metadataRepository;
    private final QuestionsRepository questionsRepository;
    private final TagRepository tagRepository;

    public PotdMetadataAdminService(
            PotdProblemMetadataRepository metadataRepository,
            QuestionsRepository questionsRepository,
            TagRepository tagRepository) {
        this.metadataRepository = metadataRepository;
        this.questionsRepository = questionsRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public MetadataResponse upsert(long questionId, UpsertMetadataRequest request, String reviewer) {
        Question question = questionsRepository
                .findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        PotdProblemMetadata meta = metadataRepository.findById(questionId).orElse(null);
        Tag primaryTag = null;
        if (request.getPrimaryTagId() != null) {
            primaryTag = tagRepository
                    .findById(request.getPrimaryTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("Primary tag not found"));
        }
        Date now = new Date();
        if (meta == null) {
            meta = PotdProblemMetadata.builder()
                    .question(question)
                    .build();
        }
        meta.setEligible(request.isEligible());
        meta.setCurationStatus(request.getCurationStatus());
        meta.setQualityScore(request.getQualityScore());
        meta.setPrimaryTag(primaryTag);
        meta.setFamilyKey(request.getFamilyKey());
        meta.setCooldownDaysOverride(request.getCooldownDaysOverride());
        meta.setValidationStatus(request.getValidationStatus());
        meta.setReviewedAt(now);
        meta.setReviewedBy(reviewer);
        if (request.getValidationStatus() == com.hrishabh.problemservice.dailychallenge.model.PotdValidationStatus.VALID) {
            meta.setValidatedAt(now);
        }
        metadataRepository.save(meta);
        return toResponse(meta);
    }

    @Transactional(readOnly = true)
    public MetadataResponse get(long questionId) {
        PotdProblemMetadata meta = metadataRepository
                .findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("POTD metadata not found"));
        return toResponse(meta);
    }

    private MetadataResponse toResponse(PotdProblemMetadata meta) {
        return MetadataResponse.builder()
                .questionId(meta.getQuestionId())
                .eligible(meta.isEligible())
                .curationStatus(meta.getCurationStatus())
                .qualityScore(meta.getQualityScore())
                .primaryTagId(meta.getPrimaryTag() != null ? meta.getPrimaryTag().getId() : null)
                .familyKey(meta.getFamilyKey())
                .cooldownDaysOverride(meta.getCooldownDaysOverride())
                .validationStatus(meta.getValidationStatus())
                .build();
    }
}
