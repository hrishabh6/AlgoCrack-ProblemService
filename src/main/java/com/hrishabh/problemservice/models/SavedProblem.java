package com.hrishabh.problemservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

/**
 * A problem in a user's built-in "Saved" collection.
 * {@code userId} is the AuthService business id; users live in another database, so there is no FK.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "saved_problem", uniqueConstraints = @UniqueConstraint(
        name = "uk_saved_problem_user_question", columnNames = {"user_id", "question_id"}))
public class SavedProblem extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;
}
