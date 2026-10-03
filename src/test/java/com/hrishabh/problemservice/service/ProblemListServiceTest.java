package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.dto.ProblemListDetailDto;
import com.hrishabh.problemservice.dto.ProblemListRequestDto;
import com.hrishabh.problemservice.dto.ProblemListSummaryDto;
import com.hrishabh.problemservice.dto.ProblemListsOverviewDto;
import com.hrishabh.problemservice.exceptions.ConflictException;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.models.ProblemList;
import com.hrishabh.problemservice.models.ProblemListItem;
import com.hrishabh.problemservice.models.SavedProblem;
import com.hrishabh.problemservice.repository.ProblemListItemRepository;
import com.hrishabh.problemservice.repository.ProblemListRepository;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.SavedProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemListServiceTest {

    private static final String OWNER = "user-alice";
    private static final String INTRUDER = "user-mallory";

    @Mock
    private ProblemListRepository listRepository;
    @Mock
    private ProblemListItemRepository itemRepository;
    @Mock
    private SavedProblemRepository savedRepository;
    @Mock
    private QuestionsRepository questionsRepository;
    @Mock
    private QuestionService questionService;

    @InjectMocks
    private ProblemListService service;

    private ProblemList aliceList;

    @BeforeEach
    void setUp() {
        aliceList = ProblemList.builder().userId(OWNER).name("Graphs").build();
        aliceList.setId(7L);
    }

    private void listOwnedByAlice() {
        when(listRepository.findByIdAndUserId(7L, OWNER)).thenReturn(Optional.of(aliceList));
    }

    private void listInvisibleTo(String userId) {
        when(listRepository.findByIdAndUserId(7L, userId)).thenReturn(Optional.empty());
    }

    @Nested
    class CreateList {

        @Test
        void createsTrimmedListForCaller() {
            when(listRepository.countByUserId(OWNER)).thenReturn(0L);
            when(listRepository.existsByUserIdAndNameIgnoreCase(OWNER, "Interview Prep")).thenReturn(false);
            when(listRepository.save(any(ProblemList.class))).thenAnswer(inv -> {
                ProblemList l = inv.getArgument(0);
                l.setId(11L);
                return l;
            });

            ProblemListSummaryDto created = service.createList(OWNER,
                    new ProblemListRequestDto("  Interview   Prep ", "  for onsite  "));

            ArgumentCaptor<ProblemList> saved = ArgumentCaptor.forClass(ProblemList.class);
            verify(listRepository).save(saved.capture());
            assertThat(saved.getValue().getUserId()).isEqualTo(OWNER);
            assertThat(saved.getValue().getName()).isEqualTo("Interview Prep");
            assertThat(saved.getValue().getDescription()).isEqualTo("for onsite");
            assertThat(created.getId()).isEqualTo(11L);
            assertThat(created.getProblemCount()).isZero();
            assertThat(created.getProblemIds()).isEmpty();
        }

        @Test
        void rejectsDuplicateNameCaseInsensitively() {
            when(listRepository.countByUserId(OWNER)).thenReturn(1L);
            when(listRepository.existsByUserIdAndNameIgnoreCase(OWNER, "graphs")).thenReturn(true);

            assertThatThrownBy(() -> service.createList(OWNER, new ProblemListRequestDto("graphs", null)))
                    .isInstanceOf(ConflictException.class);
            verify(listRepository, never()).save(any());
        }

        @Test
        void reservesTheSavedName() {
            when(listRepository.countByUserId(OWNER)).thenReturn(0L);

            assertThatThrownBy(() -> service.createList(OWNER, new ProblemListRequestDto("saved", null)))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void rejectsBlankAndOverlongNames() {
            assertThatThrownBy(() -> service.createList(OWNER, new ProblemListRequestDto("   ", null)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.createList(OWNER, new ProblemListRequestDto("x".repeat(61), null)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.createList(OWNER,
                    new ProblemListRequestDto("ok", "d".repeat(281))))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(listRepository, never()).save(any());
        }

        @Test
        void enforcesPerUserListLimit() {
            when(listRepository.countByUserId(OWNER)).thenReturn((long) ProblemListService.MAX_LISTS_PER_USER);

            assertThatThrownBy(() -> service.createList(OWNER, new ProblemListRequestDto("One more", null)))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    class RenameAndDelete {

        @Test
        void renamesOwnedList() {
            listOwnedByAlice();
            when(listRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(OWNER, "Graph Revision", 7L))
                    .thenReturn(false);
            when(listRepository.save(aliceList)).thenReturn(aliceList);
            when(itemRepository.findQuestionIdsByListId(7L)).thenReturn(List.of(3L, 1L));

            ProblemListSummaryDto updated = service.updateList(OWNER, 7L,
                    new ProblemListRequestDto("Graph Revision", ""));

            assertThat(updated.getName()).isEqualTo("Graph Revision");
            assertThat(updated.getDescription()).isNull();
            assertThat(updated.getProblemIds()).containsExactly(3L, 1L);
        }

        @Test
        void rejectsRenameToAnotherExistingName() {
            listOwnedByAlice();
            when(listRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(OWNER, "Blind 75", 7L)).thenReturn(true);

            assertThatThrownBy(() -> service.updateList(OWNER, 7L, new ProblemListRequestDto("Blind 75", null)))
                    .isInstanceOf(ConflictException.class);
            verify(listRepository, never()).save(any());
        }

        @Test
        void cannotRenameAnotherUsersList() {
            listInvisibleTo(INTRUDER);

            assertThatThrownBy(() -> service.updateList(INTRUDER, 7L, new ProblemListRequestDto("Mine now", null)))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(listRepository, never()).save(any());
        }

        @Test
        void deletesOwnedList() {
            listOwnedByAlice();

            service.deleteList(OWNER, 7L);

            verify(listRepository).delete(aliceList);
        }

        @Test
        void cannotDeleteAnotherUsersList() {
            listInvisibleTo(INTRUDER);

            assertThatThrownBy(() -> service.deleteList(INTRUDER, 7L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(listRepository, never()).delete(any());
        }

        @Test
        void unknownListIsNotFound() {
            when(listRepository.findByIdAndUserId(999L, OWNER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getList(OWNER, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class ListMembership {

        @Test
        void addsProblemToOwnedList() {
            listOwnedByAlice();
            when(questionsRepository.existsById(42L)).thenReturn(true);
            when(itemRepository.existsByListIdAndQuestionId(7L, 42L)).thenReturn(false);
            when(itemRepository.countByListId(7L)).thenReturn(0L);

            service.addProblemToList(OWNER, 7L, 42L);

            ArgumentCaptor<ProblemListItem> item = ArgumentCaptor.forClass(ProblemListItem.class);
            verify(itemRepository).save(item.capture());
            assertThat(item.getValue().getList()).isSameAs(aliceList);
            assertThat(item.getValue().getQuestionId()).isEqualTo(42L);
        }

        @Test
        void duplicateAddIsANoOp() {
            listOwnedByAlice();
            when(questionsRepository.existsById(42L)).thenReturn(true);
            when(itemRepository.existsByListIdAndQuestionId(7L, 42L)).thenReturn(true);

            service.addProblemToList(OWNER, 7L, 42L);

            verify(itemRepository, never()).save(any());
        }

        @Test
        void rejectsUnknownProblem() {
            listOwnedByAlice();
            when(questionsRepository.existsById(404L)).thenReturn(false);

            assertThatThrownBy(() -> service.addProblemToList(OWNER, 7L, 404L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Problem");
            verify(itemRepository, never()).save(any());
        }

        @Test
        void cannotAddToAnotherUsersList() {
            listInvisibleTo(INTRUDER);

            assertThatThrownBy(() -> service.addProblemToList(INTRUDER, 7L, 42L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(itemRepository, never()).save(any());
        }

        @Test
        void removesProblemFromOwnedList() {
            listOwnedByAlice();
            when(itemRepository.deleteByListIdAndQuestionId(7L, 42L)).thenReturn(1);

            service.removeProblemFromList(OWNER, 7L, 42L);

            verify(itemRepository).deleteByListIdAndQuestionId(7L, 42L);
        }

        @Test
        void cannotRemoveFromAnotherUsersList() {
            listInvisibleTo(INTRUDER);

            assertThatThrownBy(() -> service.removeProblemFromList(INTRUDER, 7L, 42L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(itemRepository, never()).deleteByListIdAndQuestionId(any(), any());
        }

        @Test
        void listDetailKeepsMostRecentlyAddedOrder() {
            listOwnedByAlice();
            when(itemRepository.findQuestionIdsByListId(7L)).thenReturn(List.of(5L, 2L));
            when(questionsRepository.findAllById(List.of(5L, 2L))).thenReturn(List.of());
            when(questionService.toSummaries(anyList())).thenReturn(List.of());

            ProblemListDetailDto detail = service.getList(OWNER, 7L);

            assertThat(detail.getProblemCount()).isEqualTo(2);
            assertThat(detail.isBuiltIn()).isFalse();
            assertThat(detail.getName()).isEqualTo("Graphs");
        }
    }

    @Nested
    class SavedCollection {

        @Test
        void savesProblemForCaller() {
            when(questionsRepository.existsById(42L)).thenReturn(true);
            when(savedRepository.existsByUserIdAndQuestionId(OWNER, 42L)).thenReturn(false);
            when(savedRepository.countByUserId(OWNER)).thenReturn(0L);

            service.saveProblem(OWNER, 42L);

            ArgumentCaptor<SavedProblem> saved = ArgumentCaptor.forClass(SavedProblem.class);
            verify(savedRepository).save(saved.capture());
            assertThat(saved.getValue().getUserId()).isEqualTo(OWNER);
            assertThat(saved.getValue().getQuestionId()).isEqualTo(42L);
        }

        @Test
        void savingTwiceIsANoOp() {
            when(questionsRepository.existsById(42L)).thenReturn(true);
            when(savedRepository.existsByUserIdAndQuestionId(OWNER, 42L)).thenReturn(true);

            service.saveProblem(OWNER, 42L);

            verify(savedRepository, never()).save(any());
        }

        @Test
        void cannotSaveUnknownProblem() {
            when(questionsRepository.existsById(404L)).thenReturn(false);

            assertThatThrownBy(() -> service.saveProblem(OWNER, 404L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void unsaveOnlyTouchesCallersRow() {
            service.unsaveProblem(OWNER, 42L);

            verify(savedRepository).deleteByUserIdAndQuestionId(OWNER, 42L);
        }

        @Test
        void savedDetailIsBuiltIn() {
            when(savedRepository.findQuestionIdsByUserId(OWNER)).thenReturn(List.of());
            when(questionService.toSummaries(anyList())).thenReturn(List.of());

            ProblemListDetailDto saved = service.getSaved(OWNER);

            assertThat(saved.isBuiltIn()).isTrue();
            assertThat(saved.getId()).isNull();
            assertThat(saved.getName()).isEqualTo(ProblemListService.SAVED_NAME);
        }
    }

    @Test
    void overviewGroupsMembershipPerListWithoutPerListQueries() {
        ProblemList second = ProblemList.builder().userId(OWNER).name("Hard retries").build();
        second.setId(8L);
        when(savedRepository.findQuestionIdsByUserId(OWNER)).thenReturn(List.of(4L, 1L));
        when(itemRepository.findMembershipByUserId(OWNER)).thenReturn(List.of(
                new Object[]{7L, 3L}, new Object[]{7L, 1L}, new Object[]{8L, 9L}));
        when(listRepository.findByUserIdOrderByCreatedAtAscIdAsc(OWNER)).thenReturn(List.of(aliceList, second));

        ProblemListsOverviewDto overview = service.getOverview(OWNER);

        assertThat(overview.getSaved().getProblemIds()).containsExactly(4L, 1L);
        assertThat(overview.getLists()).hasSize(2);
        assertThat(overview.getLists().get(0).getProblemIds()).containsExactly(3L, 1L);
        assertThat(overview.getLists().get(1).getProblemIds()).containsExactly(9L);
        verify(itemRepository, never()).findQuestionIdsByListId(any());
    }
}
