package com.hrishabh.problemservice.dailychallenge.controller;

import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.DailyChallengeRangeResponse;
import com.hrishabh.problemservice.dailychallenge.dto.DailyChallengePublicDtos.DailyChallengeResponse;
import com.hrishabh.problemservice.dailychallenge.service.DailyChallengeReadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-challenges")
public class DailyChallengeController {

    private final DailyChallengeReadService readService;

    public DailyChallengeController(DailyChallengeReadService readService) {
        this.readService = readService;
    }

    @GetMapping("/today")
    public ResponseEntity<DailyChallengeResponse> today() {
        return ResponseEntity.ok(readService.getToday());
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyChallengeResponse> byDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(readService.getByDate(date));
    }

    @GetMapping
    public ResponseEntity<DailyChallengeRangeResponse> range(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(readService.getRange(from, to));
    }
}
