package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestcasesRepository extends JpaRepository<TestCase, Long> {
}
