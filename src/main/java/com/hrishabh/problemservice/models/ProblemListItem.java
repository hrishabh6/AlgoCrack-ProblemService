package com.hrishabh.problemservice.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Membership of a question in a {@link ProblemList}. Rows are removed by FK cascade when
 * either the list or the question is deleted.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "problem_list_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_problem_list_item_list_question", columnNames = {"list_id", "question_id"}))
public class ProblemListItem extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "list_id", nullable = false)
    private ProblemList list;

    @Column(name = "question_id", nullable = false)
    private Long questionId;
}
