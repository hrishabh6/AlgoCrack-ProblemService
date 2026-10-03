package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.dto.ProblemListDetailDto;
import com.hrishabh.problemservice.dto.ProblemListRequestDto;
import com.hrishabh.problemservice.dto.ProblemListSummaryDto;
import com.hrishabh.problemservice.dto.ProblemListsOverviewDto;
import com.hrishabh.problemservice.exceptions.ConflictException;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.models.ProblemList;
import com.hrishabh.problemservice.models.ProblemListItem;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.SavedProblem;
import com.hrishabh.problemservice.repository.ProblemListItemRepository;
import com.hrishabh.problemservice.repository.ProblemListRepository;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.SavedProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User-owned problem collections: the built-in Saved collection and custom lists.
 *
 * <p>Every method takes the caller's user id as resolved from the gateway-injected identity header.
 * Lists are always looked up by {@code (id, userId)}, so another user's list is indistinguishable
 * from a missing one (404) and can never be read or modified.</p>
 */
@Service
@RequiredArgsConstructor
public class ProblemListService {

    public static final String SAVED_NAME = "Saved";
    public static final int MAX_LISTS_PER_USER = 100;
    public static final int MAX_PROBLEMS_PER_COLLECTION = 500;

    private final ProblemListRepository listRepository;
    private final ProblemListItemRepository itemRepository;
    private final SavedProblemRepository savedRepository;
    private final QuestionsRepository questionsRepository;
    private final QuestionService questionService;

    // ── Overview ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProblemListsOverviewDto getOverview(String userId) {
        List<Long> savedIds = savedRepository.findQuestionIdsByUserId(userId);

        Map<Long, List<Long>> membership = new HashMap<>();
        for (Object[] row : itemRepository.findMembershipByUserId(userId)) {
            membership.computeIfAbsent((Long) row[0], k -> new ArrayList<>()).add((Long) row[1]);
        }

        List<ProblemListSummaryDto> lists = listRepository.findByUserIdOrderByCreatedAtAscIdAsc(userId).stream()
                .map(list -> toSummary(list, membership.getOrDefault(list.getId(), List.of())))
                .toList();

        return ProblemListsOverviewDto.builder()
                .saved(ProblemListsOverviewDto.SavedCollection.builder()
                        .problemCount(savedIds.size())
                        .problemIds(savedIds)
                        .build())
                .lists(lists)
                .build();
    }

    // ── Custom lists ────────────────────────────────────────────────────

    @Transactional
    public ProblemListSummaryDto createList(String userId, ProblemListRequestDto request) {
        String name = normalizeName(request == null ? null : request.getName());
        String description = normalizeDescription(request == null ? null : request.getDescription());

        if (listRepository.countByUserId(userId) >= MAX_LISTS_PER_USER) {
            throw new ConflictException("You can have at most " + MAX_LISTS_PER_USER + " lists");
        }
        if (SAVED_NAME.equalsIgnoreCase(name) || listRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ConflictException("You already have a list named \"" + name + "\"");
        }

        ProblemList saved = listRepository.save(ProblemList.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .build());
        return toSummary(saved, List.of());
    }

    @Transactional(readOnly = true)
    public ProblemListDetailDto getList(String userId, Long listId) {
        ProblemList list = requireOwnedList(userId, listId);
        List<Long> ids = itemRepository.findQuestionIdsByListId(list.getId());
        return ProblemListDetailDto.builder()
                .id(list.getId())
                .name(list.getName())
                .description(list.getDescription())
                .builtIn(false)
                .problemCount(ids.size())
                .createdAt(list.getCreatedAt())
                .updatedAt(list.getUpdatedAt())
                .problems(questionService.toSummaries(loadInOrder(ids)))
                .build();
    }

    @Transactional
    public ProblemListSummaryDto updateList(String userId, Long listId, ProblemListRequestDto request) {
        ProblemList list = requireOwnedList(userId, listId);
        if (request == null || (request.getName() == null && request.getDescription() == null)) {
            throw new IllegalArgumentException("Nothing to update");
        }

        if (request.getName() != null) {
            String name = normalizeName(request.getName());
            if (SAVED_NAME.equalsIgnoreCase(name)
                    || listRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, list.getId())) {
                throw new ConflictException("You already have a list named \"" + name + "\"");
            }
            list.setName(name);
        }
        if (request.getDescription() != null) {
            list.setDescription(normalizeDescription(request.getDescription()));
        }
        list.setUpdatedAt(new Date());

        ProblemList saved = listRepository.save(list);
        return toSummary(saved, itemRepository.findQuestionIdsByListId(saved.getId()));
    }

    @Transactional
    public void deleteList(String userId, Long listId) {
        ProblemList list = requireOwnedList(userId, listId);
        // problem_list_item rows are removed by ON DELETE CASCADE.
        listRepository.delete(list);
    }

    @Transactional
    public void addProblemToList(String userId, Long listId, Long problemId) {
        ProblemList list = requireOwnedList(userId, listId);
        requireProblem(problemId);
        if (itemRepository.existsByListIdAndQuestionId(list.getId(), problemId)) {
            return;
        }
        if (itemRepository.countByListId(list.getId()) >= MAX_PROBLEMS_PER_COLLECTION) {
            throw new ConflictException("A list can hold at most " + MAX_PROBLEMS_PER_COLLECTION + " problems");
        }
        itemRepository.save(ProblemListItem.builder().list(list).questionId(problemId).build());
        list.setUpdatedAt(new Date());
        listRepository.save(list);
    }

    @Transactional
    public void removeProblemFromList(String userId, Long listId, Long problemId) {
        ProblemList list = requireOwnedList(userId, listId);
        if (itemRepository.deleteByListIdAndQuestionId(list.getId(), problemId) > 0) {
            list.setUpdatedAt(new Date());
            listRepository.save(list);
        }
    }

    // ── Saved collection ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProblemListDetailDto getSaved(String userId) {
        List<Long> ids = savedRepository.findQuestionIdsByUserId(userId);
        return ProblemListDetailDto.builder()
                .id(null)
                .name(SAVED_NAME)
                .description("Problems you bookmarked to come back to.")
                .builtIn(true)
                .problemCount(ids.size())
                .problems(questionService.toSummaries(loadInOrder(ids)))
                .build();
    }

    @Transactional
    public void saveProblem(String userId, Long problemId) {
        requireProblem(problemId);
        if (savedRepository.existsByUserIdAndQuestionId(userId, problemId)) {
            return;
        }
        if (savedRepository.countByUserId(userId) >= MAX_PROBLEMS_PER_COLLECTION) {
            throw new ConflictException("You can save at most " + MAX_PROBLEMS_PER_COLLECTION + " problems");
        }
        savedRepository.save(SavedProblem.builder().userId(userId).questionId(problemId).build());
    }

    @Transactional
    public void unsaveProblem(String userId, Long problemId) {
        savedRepository.deleteByUserIdAndQuestionId(userId, problemId);
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private ProblemList requireOwnedList(String userId, Long listId) {
        return listRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
    }

    private void requireProblem(Long problemId) {
        if (problemId == null || !questionsRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found");
        }
    }

    /** Loads questions and returns them in the order of {@code ids}, skipping ids that no longer exist. */
    private List<Question> loadInOrder(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Question> byId = questionsRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    static String normalizeName(String raw) {
        String name = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("List name is required");
        }
        if (name.length() > ProblemList.NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "List name must be at most " + ProblemList.NAME_MAX_LENGTH + " characters");
        }
        return name;
    }

    static String normalizeDescription(String raw) {
        if (raw == null) {
            return null;
        }
        String description = raw.trim();
        if (description.isEmpty()) {
            return null;
        }
        if (description.length() > ProblemList.DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Description must be at most " + ProblemList.DESCRIPTION_MAX_LENGTH + " characters");
        }
        return description;
    }

    private ProblemListSummaryDto toSummary(ProblemList list, List<Long> problemIds) {
        return ProblemListSummaryDto.builder()
                .id(list.getId())
                .name(list.getName())
                .description(list.getDescription())
                .problemCount(problemIds.size())
                .problemIds(problemIds)
                .createdAt(list.getCreatedAt())
                .updatedAt(list.getUpdatedAt())
                .build();
    }
}
