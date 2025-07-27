package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionsRepository extends JpaRepository<Submission, Long> {
}
