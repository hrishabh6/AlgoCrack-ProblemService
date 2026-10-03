package com.hrishabh.problemservice.dailychallenge.controller;

import com.hrishabh.problemservice.dailychallenge.dto.PotdMetadataAdminDtos.MetadataResponse;
import com.hrishabh.problemservice.dailychallenge.dto.PotdMetadataAdminDtos.UpsertMetadataRequest;
import com.hrishabh.problemservice.dailychallenge.service.PotdMetadataAdminService;
import com.hrishabh.problemservice.helper.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/potd-problems")
public class PotdMetadataAdminController {

    private final PotdMetadataAdminService metadataAdminService;

    public PotdMetadataAdminController(PotdMetadataAdminService metadataAdminService) {
        this.metadataAdminService = metadataAdminService;
    }

    @GetMapping("/{questionId}/metadata")
    public ResponseEntity<MetadataResponse> get(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable long questionId) {
        CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(metadataAdminService.get(questionId));
    }

    @PutMapping("/{questionId}/metadata")
    public ResponseEntity<MetadataResponse> upsert(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable long questionId,
            @Valid @RequestBody UpsertMetadataRequest request) {
        String reviewer = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(metadataAdminService.upsert(questionId, request, reviewer));
    }
}
