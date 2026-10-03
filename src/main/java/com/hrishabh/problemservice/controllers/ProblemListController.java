package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.ProblemListDetailDto;
import com.hrishabh.problemservice.dto.ProblemListRequestDto;
import com.hrishabh.problemservice.dto.ProblemListSummaryDto;
import com.hrishabh.problemservice.dto.ProblemListsOverviewDto;
import com.hrishabh.problemservice.helper.CurrentUser;
import com.hrishabh.problemservice.service.ProblemListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The caller's Saved collection and custom problem lists. Identity always comes from the
 * gateway-injected {@code X-User-Id} header.
 */
@RestController
@RequestMapping("/api/v1/problem-lists")
@RequiredArgsConstructor
public class ProblemListController {

    private final ProblemListService problemListService;

    /**
     * Saved collection and all lists, each with member problem ids.
     */
    @GetMapping
    public ResponseEntity<ProblemListsOverviewDto> getOverview(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(problemListService.getOverview(CurrentUser.require(userId)));
    }

    @PostMapping
    public ResponseEntity<ProblemListSummaryDto> createList(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestBody(required = false) ProblemListRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemListService.createList(CurrentUser.require(userId), request));
    }

    // Literal "saved" mappings take precedence over the numeric {listId} mappings below.

    @GetMapping("/saved")
    public ResponseEntity<ProblemListDetailDto> getSaved(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(problemListService.getSaved(CurrentUser.require(userId)));
    }

    @PutMapping("/saved/problems/{problemId}")
    public ResponseEntity<Void> saveProblem(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long problemId) {
        problemListService.saveProblem(CurrentUser.require(userId), problemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/saved/problems/{problemId}")
    public ResponseEntity<Void> unsaveProblem(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long problemId) {
        problemListService.unsaveProblem(CurrentUser.require(userId), problemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ProblemListDetailDto> getList(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long listId) {
        return ResponseEntity.ok(problemListService.getList(CurrentUser.require(userId), listId));
    }

    @PatchMapping("/{listId}")
    public ResponseEntity<ProblemListSummaryDto> updateList(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long listId,
            @RequestBody(required = false) ProblemListRequestDto request) {
        return ResponseEntity.ok(problemListService.updateList(CurrentUser.require(userId), listId, request));
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long listId) {
        problemListService.deleteList(CurrentUser.require(userId), listId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Idempotent: adding a problem that is already in the list is a no-op.
     */
    @PutMapping("/{listId}/problems/{problemId}")
    public ResponseEntity<Void> addProblem(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long listId,
            @PathVariable Long problemId) {
        problemListService.addProblemToList(CurrentUser.require(userId), listId, problemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{listId}/problems/{problemId}")
    public ResponseEntity<Void> removeProblem(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @PathVariable Long listId,
            @PathVariable Long problemId) {
        problemListService.removeProblemFromList(CurrentUser.require(userId), listId, problemId);
        return ResponseEntity.noContent().build();
    }
}
