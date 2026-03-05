package com.hrishabh.problemservice.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Reference solution (oracle) for a question.
 * Used to compute expected output dynamically during judging.
 * Not exposed to users.
 * 
 * There is exactly ONE oracle per question (enforced by OneToOne).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reference_solution")
public class ReferenceSolution extends BaseModel {

    /**
     * The question this reference solution belongs to (1:1 relationship)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private Question question;

    /**
     * Programming language of this reference solution.
     * Only one language needed - the oracle must be deterministic.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    /**
     * The reference solution source code (oracle)
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceCode;
}
