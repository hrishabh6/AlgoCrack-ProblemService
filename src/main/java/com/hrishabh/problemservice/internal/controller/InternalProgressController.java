package com.hrishabh.problemservice.internal.controller;

import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.PotdResolveResponse;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.RankMetadataBatchRequest;
import com.hrishabh.problemservice.internal.dto.QuestionRankMetadataDtos.RankMetadataBatchResponse;
import com.hrishabh.problemservice.internal.service.PotdCompletionResolveService;
import com.hrishabh.problemservice.internal.service.QuestionRankMetadataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalProgressController {

    private final QuestionRankMetadataService questionRankMetadataService;
    private final PotdCompletionResolveService potdCompletionResolveService;

    public InternalProgressController(
            QuestionRankMetadataService questionRankMetadataService,
            PotdCompletionResolveService potdCompletionResolveService) {
        this.questionRankMetadataService = questionRankMetadataService;
        this.potdCompletionResolveService = potdCompletionResolveService;
    }

    @PostMapping("/questions/rank-metadata")
    public ResponseEntity<RankMetadataBatchResponse> rankMetadata(
            @Valid @RequestBody RankMetadataBatchRequest request) {
        return ResponseEntity.ok(questionRankMetadataService.fetchBatch(request.questionIds()));
    }

    @GetMapping("/daily-challenges/resolve")
    public ResponseEntity<PotdResolveResponse> resolvePotdCompletion(
            @RequestParam long questionId,
            @RequestParam Instant completedAt) {
        return ResponseEntity.ok(potdCompletionResolveService.resolve(questionId, completedAt));
    }
}
