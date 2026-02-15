package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionsRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
}
