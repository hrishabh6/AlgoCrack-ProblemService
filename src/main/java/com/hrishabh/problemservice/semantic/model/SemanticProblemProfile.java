package com.hrishabh.problemservice.semantic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "semantic_problem_profile",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_semantic_question_version", columnNames = {"questionId", "semanticVersion"}),
                @UniqueConstraint(name = "uk_semantic_question_active_slot", columnNames = {"questionId", "activeSlot"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanticProblemProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false, unique = true, length = 64)
    private String profileId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "semantic_schema_version", nullable = false, length = 32)
    private String semanticSchemaVersion;

    @Column(name = "semantic_version", nullable = false, length = 32)
    private String semanticVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SemanticProfileStatus status;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "active_slot", insertable = false, updatable = false)
    private Integer activeSlot;

    @Column(name = "profile_json", nullable = false, columnDefinition = "json")
    private String profileJson;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "profile_hash", nullable = false, length = 64)
    private String profileHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
