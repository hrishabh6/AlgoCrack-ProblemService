package com.hrishabh.problemservice.dailychallenge.controller;

import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengeAdminDtos.*;
import com.hrishabh.problemservice.dailychallenge.model.DailyChallengeStatus;
import com.hrishabh.problemservice.dailychallenge.service.DailyChallengeAdminService;
import com.hrishabh.problemservice.dailychallenge.service.DailyChallengeAuditService;
import com.hrishabh.problemservice.helper.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/daily-challenges")
public class DailyChallengeAdminController {

    private final DailyChallengeAdminService adminService;
    private final DailyChallengeAuditService auditService;

    public DailyChallengeAdminController(
            DailyChallengeAdminService adminService, DailyChallengeAuditService auditService) {
        this.adminService = adminService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<AdminChallengeListResponse> list(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) DailyChallengeStatus status) {
        CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.list(from, to, status));
    }

    @PostMapping("/generations")
    public ResponseEntity<GenerationResponse> generate(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @Valid @RequestBody GenerationRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.generate(request, actor));
    }

    @PostMapping("/publications")
    public ResponseEntity<PublicationResponse> publish(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @Valid @RequestBody PublicationRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.publish(request, actor));
    }

    @PutMapping("/{date}")
    public ResponseEntity<AdminChallengeRow> replace(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ManualReplaceRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.manualReplace(date, request, actor));
    }

    @PostMapping("/{date}/lock")
    public ResponseEntity<AdminChallengeRow> lock(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ReasonRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.lock(date, request, actor));
    }

    @PostMapping("/{date}/unlock")
    public ResponseEntity<AdminChallengeRow> unlock(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ReasonRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.unlock(date, request, actor));
    }

    @PostMapping("/{date}/cancel")
    public ResponseEntity<AdminChallengeRow> cancel(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ReasonRequest request) {
        String actor = CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.cancel(date, request, actor));
    }

    @GetMapping("/preflight")
    public ResponseEntity<PreflightResponse> preflight(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role) {
        CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(adminService.preflight());
    }

    @GetMapping("/{date}/audit")
    public ResponseEntity<AuditListResponse> audit(
            @RequestHeader(value = CurrentUser.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = CurrentUser.ROLE_HEADER, required = false) String role,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CurrentUser.requireAdmin(userId, role);
        return ResponseEntity.ok(auditService.listForDate(date));
    }
}
