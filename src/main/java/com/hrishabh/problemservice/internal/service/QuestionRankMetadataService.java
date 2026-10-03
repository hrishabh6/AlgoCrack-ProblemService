package com.hrishabh.problemservice.internal.service;

import com.hrishabh.problemservice.internal.RankDifficultyNormalizer;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.QuestionRankMetadataItem;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.RankMetadataBatchResponse;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.TagRef;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionRankMetadataService {

    public static final int MAX_BATCH_SIZE = 200;

    private final QuestionsRepository questionsRepository;

    public QuestionRankMetadataService(QuestionsRepository questionsRepository) {
        this.questionsRepository = questionsRepository;
    }

    @Transactional(readOnly = true)
    public RankMetadataBatchResponse fetchBatch(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new IllegalArgumentException("questionIds must not be empty");
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>(questionIds);
        if (unique.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("questionIds exceeds cap of " + MAX_BATCH_SIZE);
        }

        List<Question> loaded = questionsRepository.findByIdInWithTags(unique);
        Map<Long, Question> byId = loaded.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

        List<QuestionRankMetadataItem> items = new ArrayList<>(unique.size());
        for (Long id : unique) {
            Question q = byId.get(id);
            if (q == null) {
                items.add(QuestionRankMetadataItem.builder()
                        .questionId(id)
                        .questionStatus("NOT_FOUND")
                        .difficultyLevel(null)
                        .normalizedDifficulty("UNKNOWN")
                        .tags(List.of())
                        .build());
                continue;
            }
            items.add(toItem(q));
        }
        return RankMetadataBatchResponse.builder().items(items).build();
    }

    private static QuestionRankMetadataItem toItem(Question q) {
        List<TagRef> tags = q.getTags() == null ? List.of() : q.getTags().stream()
                .map(QuestionRankMetadataService::toTagRef)
                .toList();
        String rawDifficulty = q.getDifficultyLevel();
        return QuestionRankMetadataItem.builder()
                .questionId(q.getId())
                .questionStatus(q.getStatus() != null ? q.getStatus().name() : "UNKNOWN")
                .difficultyLevel(rawDifficulty)
                .normalizedDifficulty(RankDifficultyNormalizer.normalize(rawDifficulty))
                .tags(tags)
                .build();
    }

    private static TagRef toTagRef(Tag tag) {
        return TagRef.builder()
                .tagId(tag.getId())
                .tagName(tag.getName())
                .build();
    }
}
