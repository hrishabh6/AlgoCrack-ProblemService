package com.hrishabh.problemservice.dailychallenge.service;

import com.hrishabh.problemservice.dailychallenge.exception.PotdUnprocessableException;
import com.hrishabh.problemservice.dailychallenge.model.PotdCurationStatus;
import com.hrishabh.problemservice.dailychallenge.model.PotdProblemMetadata;
import com.hrishabh.problemservice.dailychallenge.model.PotdValidationStatus;
import com.hrishabh.problemservice.dailychallenge.repository.PotdProblemMetadataRepository;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.QuestionStatus;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import org.springframework.stereotype.Service;

@Service
public class PotdEligibilityService {

    private final QuestionsRepository questionsRepository;
    private final PotdProblemMetadataRepository metadataRepository;

    public PotdEligibilityService(
            QuestionsRepository questionsRepository, PotdProblemMetadataRepository metadataRepository) {
        this.questionsRepository = questionsRepository;
        this.metadataRepository = metadataRepository;
    }

    public Question requireEligibleQuestion(long questionId) {
        Question question = questionsRepository
                .findByIdWithJudgingAssets(questionId)
                .orElseThrow(() -> new PotdUnprocessableException("Question not found: " + questionId));
        PotdProblemMetadata meta = metadataRepository
                .findById(questionId)
                .orElseThrow(() -> new PotdUnprocessableException("POTD metadata missing for question " + questionId));
        validate(question, meta);
        return question;
    }

    public boolean isStructurallyComplete(Question question) {
        return question.getReferenceSolution() != null
                && question.getTestCases() != null
                && !question.getTestCases().isEmpty();
    }

    private void validate(Question question, PotdProblemMetadata meta) {
        if (question.getStatus() != QuestionStatus.PUBLISHED) {
            throw new PotdUnprocessableException("Question is not published");
        }
        if (!meta.isEligible() || meta.getCurationStatus() != PotdCurationStatus.APPROVED) {
            throw new PotdUnprocessableException("Question is not approved for POTD");
        }
        if (meta.getValidationStatus() != PotdValidationStatus.VALID) {
            throw new PotdUnprocessableException("Question validation status is not VALID");
        }
        if (meta.getPrimaryTag() == null) {
            throw new PotdUnprocessableException("Primary tag is required for POTD");
        }
        if (!isStructurallyComplete(question)) {
            throw new PotdUnprocessableException("Question is not structurally complete for POTD");
        }
    }
}
