package com.hrishabh.problemservice.dailychallenge.repository;

import com.hrishabh.problemservice.dailychallenge.model.PotdProblemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PotdProblemMetadataRepository extends JpaRepository<PotdProblemMetadata, Long> {
}
