package com.hrishabh.problemservice.complexity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complexity_benchmark_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplexityBenchmarkProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false, unique = true, length = 64)
    private String profileId;

    @Column(name = "profile_code", nullable = false, length = 64)
    private String profileCode;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(name = "profile_version", nullable = false, length = 32)
    private String profileVersion;

    @Column(name = "generator_key", nullable = false, length = 64)
    private String generatorKey;

    @Column(name = "generator_version", nullable = false, length = 32)
    private String generatorVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ComplexityProfileStatus status;

    @Column(name = "variable_definitions_json", nullable = false, columnDefinition = "json")
    private String variableDefinitionsJson;

    @Column(name = "size_plan_json", nullable = false, columnDefinition = "json")
    private String sizePlanJson;

    @Column(name = "variant_plan_json", nullable = false, columnDefinition = "json")
    private String variantPlanJson;

    @Column(name = "measurement_limits_json", nullable = false, columnDefinition = "json")
    private String measurementLimitsJson;

    @Column(name = "profile_hash", nullable = false, length = 64)
    private String profileHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
