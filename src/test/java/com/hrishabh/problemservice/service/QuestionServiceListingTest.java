package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.client.SubmissionServiceClient;
import com.hrishabh.problemservice.controllers.ProblemsControllerTestAccess;
import com.hrishabh.problemservice.dto.QuestionQuery;
import com.hrishabh.problemservice.dto.QuestionStatsApiDto;
import com.hrishabh.problemservice.dto.QuestionSummaryDto;
import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import com.hrishabh.problemservice.repository.QuestionsRepository;
import com.hrishabh.problemservice.repository.ReferenceSolutionRepository;
import com.hrishabh.problemservice.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceListingTest {

    @Mock
    private QuestionsRepository questionsRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private ReferenceSolutionRepository referenceSolutionRepository;
    @Mock
    private SubmissionServiceClient submissionServiceClient;

    @InjectMocks
    private QuestionService questionService;

    private static Question question(long id, String title, String... tags) {
        Question q = Question.builder()
                .questionTitle(title)
                .difficultyLevel("Hard")
                .tags(new ArrayList<>(java.util.Arrays.stream(tags)
                        .map(name -> Tag.builder().name(name).build()).toList()))
                .build();
        q.setId(id);
        return q;
    }

    @Test
    void acceptanceComesFromOneBatchedStatsCall() {
        List<Question> questions = List.of(question(1, "A", "Array"), question(2, "B"), question(3, "C"));
        when(submissionServiceClient.getQuestionStats(List.of(1L, 2L, 3L))).thenReturn(Map.of(
                1L, new QuestionStatsApiDto(1L, 12, 5),
                3L, new QuestionStatsApiDto(3L, 0, 0)));

        List<QuestionSummaryDto> rows = questionService.toSummaries(questions);

        verify(submissionServiceClient, times(1)).getQuestionStats(any());
        assertThat(rows).extracting(QuestionSummaryDto::getId).containsExactly(1L, 2L, 3L);
        assertThat(rows.get(0).getAcceptanceRate()).isEqualTo(41.7);
        assertThat(rows.get(0).getTotalSubmissions()).isEqualTo(12);
        assertThat(rows.get(0).getTags()).containsExactly("Array");
        assertThat(rows.get(1).getAcceptanceRate()).isNull();
        assertThat(rows.get(2).getAcceptanceRate()).as("no evaluated submissions ⇒ no fake 0%").isNull();
    }

    @Test
    void emptyIncludeSetShortCircuitsToEmptyPage() {
        Page<QuestionSummaryDto> page = questionService.listQuestions(
                QuestionQuery.builder().page(0).size(20).build(), List.of(), null);

        assertThat(page.getTotalElements()).isZero();
        verifyNoInteractions(questionsRepository);
    }

    @Test
    void tagFilterParsingMergesLegacyParamAndDedupes() {
        QuestionQuery query = ProblemsControllerTestAccess.buildQuery(" Graph, Array ,,Graph", "Tree");

        assertThat(query.getTags()).containsExactly("Graph", "Array", "Tree");
    }

    @Test
    void excludeFiltersParseAndDedupe() {
        QuestionQuery query = ProblemsControllerTestAccess.buildExcludeQuery("Easy", " Array, Graph ,,Array");

        assertThat(query.getExcludeDifficulty()).isEqualTo("Easy");
        assertThat(query.getExcludeTags()).containsExactly("Array", "Graph");
    }
}
