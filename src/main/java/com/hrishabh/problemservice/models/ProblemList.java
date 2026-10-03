package com.hrishabh.problemservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

/**
 * A user-created collection of problems. Items live in {@link ProblemListItem}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "problem_list", uniqueConstraints = @UniqueConstraint(
        name = "uk_problem_list_user_name", columnNames = {"user_id", "name"}))
public class ProblemList extends BaseModel {

    public static final int NAME_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 280;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;
}
