package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.ProblemListRequestDto;
import com.hrishabh.problemservice.dto.ProblemListSummaryDto;
import com.hrishabh.problemservice.dto.ProblemListsOverviewDto;
import com.hrishabh.problemservice.exceptions.ConflictException;
import com.hrishabh.problemservice.exceptions.GlobalExceptionHandler;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.service.ProblemListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProblemListControllerTest {

    private static final String USER_HEADER = "X-User-Id";

    @Mock
    private ProblemListService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProblemListController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void requestsWithoutIdentityAreRejected() throws Exception {
        mvc.perform(get("/api/v1/problem-lists")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/problem-lists/saved/problems/1")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/problem-lists/7")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/problem-lists").header(USER_HEADER, "   ")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void overviewIsScopedToHeaderIdentity() throws Exception {
        when(service.getOverview("alice")).thenReturn(ProblemListsOverviewDto.builder()
                .saved(ProblemListsOverviewDto.SavedCollection.builder()
                        .problemCount(1).problemIds(List.of(3L)).build())
                .lists(List.of())
                .build());

        mvc.perform(get("/api/v1/problem-lists").header(USER_HEADER, "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved.problemIds[0]").value(3));
    }

    @Test
    void createUsesHeaderIdentityNotBody() throws Exception {
        when(service.createList(eq("alice"), any(ProblemListRequestDto.class))).thenReturn(
                ProblemListSummaryDto.builder().id(5L).name("Graphs").problemCount(0).problemIds(List.of()).build());

        mvc.perform(post("/api/v1/problem-lists")
                        .header(USER_HEADER, "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Graphs\",\"userId\":\"mallory\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Graphs"));

        verify(service).createList(eq("alice"), any(ProblemListRequestDto.class));
        verify(service, never()).createList(eq("mallory"), any());
    }

    @Test
    void duplicateNameIsConflict() throws Exception {
        when(service.createList(eq("alice"), any())).thenThrow(new ConflictException("duplicate"));

        mvc.perform(post("/api/v1/problem-lists")
                        .header(USER_HEADER, "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Graphs\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidNameIsBadRequest() throws Exception {
        when(service.createList(eq("alice"), any())).thenThrow(new IllegalArgumentException("List name is required"));

        mvc.perform(post("/api/v1/problem-lists")
                        .header(USER_HEADER, "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("List name is required"));
    }

    @Test
    void anotherUsersListLooksMissing() throws Exception {
        when(service.getList("mallory", 7L)).thenThrow(new ResourceNotFoundException("List not found"));
        doThrow(new ResourceNotFoundException("List not found")).when(service).addProblemToList("mallory", 7L, 1L);

        mvc.perform(get("/api/v1/problem-lists/7").header(USER_HEADER, "mallory"))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/problem-lists/7/problems/1").header(USER_HEADER, "mallory"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericIdsAreBadRequests() throws Exception {
        mvc.perform(get("/api/v1/problem-lists/abc").header(USER_HEADER, "alice"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/problem-lists/7/problems/xyz").header(USER_HEADER, "alice"))
                .andExpect(status().isBadRequest());
        verify(service, never()).getList(anyString(), anyLong());
    }

    @Test
    void membershipEndpointsDelegateWithCallerIdentity() throws Exception {
        mvc.perform(put("/api/v1/problem-lists/7/problems/42").header(USER_HEADER, "alice"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/problem-lists/7/problems/42").header(USER_HEADER, "alice"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/v1/problem-lists/saved/problems/42").header(USER_HEADER, "alice"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/problem-lists/saved/problems/42").header(USER_HEADER, "alice"))
                .andExpect(status().isNoContent());

        verify(service).addProblemToList("alice", 7L, 42L);
        verify(service).removeProblemFromList("alice", 7L, 42L);
        verify(service).saveProblem("alice", 42L);
        verify(service).unsaveProblem("alice", 42L);
    }

    @Test
    void unknownProblemIsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Problem not found")).when(service).saveProblem("alice", 999L);

        mvc.perform(put("/api/v1/problem-lists/saved/problems/999").header(USER_HEADER, "alice"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Problem not found"));
    }

    @Test
    void renameAndDelete() throws Exception {
        when(service.updateList(eq("alice"), eq(7L), any())).thenReturn(
                ProblemListSummaryDto.builder().id(7L).name("Renamed").problemIds(List.of()).build());

        mvc.perform(patch("/api/v1/problem-lists/7")
                        .header(USER_HEADER, "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));

        mvc.perform(delete("/api/v1/problem-lists/7").header(USER_HEADER, "alice"))
                .andExpect(status().isNoContent());
        verify(service).deleteList("alice", 7L);
    }
}
