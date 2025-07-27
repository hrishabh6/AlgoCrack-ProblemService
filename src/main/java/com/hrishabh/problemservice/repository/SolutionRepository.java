package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.Solution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
}
