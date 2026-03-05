package com.hrishabh.problemservice.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a testcase for a question.
 * Expected output is NOT stored - it is computed dynamically via the reference
 * solution (oracle).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseModel {

    /**
     * The question this testcase belongs to
     */
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    /**
     * Input for the testcase (stored as JSON string)
     */
    @Column(columnDefinition = "TEXT")
    private String input;

    /**
     * Testcase type: DEFAULT (user-visible) or HIDDEN (judge-only)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TestCaseType type;
}
