package com.hrishabh.problemservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionMetadata extends BaseModel {

    private String functionName;

    private String returnType;

    @ElementCollection
    private List<String> paramTypes = new ArrayList<>();

    @ElementCollection
    private List<String> paramNames = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(columnDefinition = "TEXT")
    private String codeTemplate;

    @Column(columnDefinition = "TEXT")
    private String testCaseFormat; // Optional: JSON string to define test case structure

    private String executionStrategy; // e.g. "function", "main-based", "class"

    private Boolean customInputEnabled;

    /**
     * For void return types: which parameter index is mutated (0-indexed).
     * e.g., "0" means the first parameter is mutated in-place.
     * Null for non-void functions.
     */
    private String mutationTarget;

    /**
     * For void return types: how to serialize the mutated parameter.
     * e.g., "LEVEL_ORDER", "ARRAY", "JSON"
     * Null for non-void functions.
     */
    private String serializationStrategy;

    /**
     * Type of question: "ALGORITHM" (default single-function) or "DESIGN_CLASS"
     * (class-based problems like Codec, LRUCache, MinStack).
     * Null is treated as ALGORITHM.
     */
    private String questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
