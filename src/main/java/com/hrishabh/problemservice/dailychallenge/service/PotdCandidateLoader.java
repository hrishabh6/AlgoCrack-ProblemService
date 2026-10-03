package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.model.PotdProblemMetadata;
import com.hrishabh.problemservice.dailychallenge.planner.PotdDifficulty;
import com.hrishabh.problemservice.dailychallenge.planner.PotdPlanningModels.PotdPlanningCandidate;
import com.hrishabh.problemservice.dailychallenge.repository.DailyChallengeRepository;
import com.hrishabh.problemservice.dailychallenge.repository.PotdEligibleQuestionRepository;
import com.hrishabh.problemservice.dailychallenge.repository.PotdProblemMetadataRepository;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PotdCandidateLoader {

    private final PotdEligibleQuestionRepository eligibleQuestionRepository;
    private final PotdProblemMetadataRepository metadataRepository;
    private final DailyChallengeRepository dailyChallengeRepository;
    private final PotdEligibilityService eligibilityService;

    public PotdCandidateLoader(
            PotdEligibleQuestionRepository eligibleQuestionRepository,
            PotdProblemMetadataRepository metadataRepository,
            DailyChallengeRepository dailyChallengeRepository,
            PotdEligibilityService eligibilityService) {
        this.eligibleQuestionRepository = eligibleQuestionRepository;
        this.metadataRepository = metadataRepository;
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.eligibilityService = eligibilityService;
    }

    @Transactional(readOnly = true)
    public List<PotdPlanningCandidate> loadEligibleCandidates() {
        List<Question> questions = eligibleQuestionRepository.findCatalogEligibleForPotd();
        List<PotdPlanningCandidate> result = new ArrayList<>();
        for (Question question : questions) {
            if (!eligibilityService.isStructurallyComplete(question)) {
                continue;
            }
            PotdProblemMetadata meta = metadataRepository.findById(question.getId()).orElse(null);
            if (meta == null) {
                continue;
            }
            result.add(toCandidate(question, meta));
        }
        return result;
    }

    private PotdPlanningCandidate toCandidate(Question question, PotdProblemMetadata meta) {
        Map<Long, Double> tagWeights = new HashMap<>();
        Long primaryId = meta.getPrimaryTag() != null ? meta.getPrimaryTag().getId() : null;
        if (primaryId != null) {
            tagWeights.put(primaryId, 1.0);
        }
        List<Tag> secondary = question.getTags().stream()
                .filter(t -> primaryId == null || t.getId() != primaryId)
                .toList();
        if (!secondary.isEmpty()) {
            double share = 0.5 / secondary.size();
            for (Tag tag : secondary) {
                tagWeights.put(tag.getId(), share);
            }
        }
        boolean neverScheduled = !dailyChallengeRepository.existsByQuestionId(question.getId());
        return new PotdPlanningCandidate(
                question.getId(),
                PotdDifficulty.fromLevel(question.getDifficultyLevel()),
                primaryId,
                tagWeights,
                meta.getFamilyKey(),
                meta.getQualityScore() / 100.0,
                neverScheduled);
    }
}
