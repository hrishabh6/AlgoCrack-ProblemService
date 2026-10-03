package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.ProblemList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemListRepository extends JpaRepository<ProblemList, Long> {

    List<ProblemList> findByUserIdOrderByCreatedAtAscIdAsc(String userId);

    Optional<ProblemList> findByIdAndUserId(Long id, String userId);

    boolean existsByUserIdAndNameIgnoreCase(String userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(String userId, String name, Long id);

    long countByUserId(String userId);
}
