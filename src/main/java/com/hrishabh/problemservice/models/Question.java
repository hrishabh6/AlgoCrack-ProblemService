package com.hrishabh.problemservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Represents a coding problem/question on the platform.
 * Owned by ProblemService.
 * Cross-service relationship (acceptedSubmissions → Submission) has been
 * removed.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseModel {

    private String questionTitle;

    @Column(columnDefinition = "TEXT")
    private String questionDescription;

    /**
     * Testcases for this question (DEFAULT and HIDDEN)
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    /**
     * Whether output array order matters during comparison
     */
    private Boolean isOutputOrderMatters;

    @ManyToMany
    @JoinTable(name = "question_tag", joinColumns = @JoinColumn(name = "question_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    private String difficultyLevel;

    private String company; // Optional

    private String constraints;

    private Integer timeoutLimit;

    /**
     * Editorial solutions visible to users (educational)
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solution> solutions = new ArrayList<>();

    /**
     * Reference solution (oracle) for computing expected output during judging.
     * Exactly ONE per question. Not exposed to users.
     */
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReferenceSolution referenceSolution;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionMetadata> metadataList = new ArrayList<>();

    /**
     * Structural hint for frontend visualization (tree, graph, list).
     * Null if not applicable.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NodeType nodeType;

    /**
     * Declarative validator hints for judging.
     * Comma-separated, nullable. e.g. "SUDOKU_RULES,EXPECT_LINEAR_FORM"
     */
    private String validationHints;
}
