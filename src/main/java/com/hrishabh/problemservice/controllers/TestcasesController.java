package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.TestCaseRequestDto;
import com.hrishabh.problemservice.service.TestcasesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/testcases")
public class TestcasesController {

    private final TestcasesService testcasesService;

    public TestcasesController(TestcasesService testcasesService) {
        this.testcasesService = testcasesService;
    }

    @PostMapping
    public ResponseEntity<Void> addTestCase(@RequestBody TestCaseRequestDto dto) {

        testcasesService.addTestCase(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // or .noContent().build() for 204
    }



}
