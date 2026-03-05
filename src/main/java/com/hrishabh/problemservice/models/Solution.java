package com.hrishabh.problemservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solution extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String language; // e.g., Java, Python, C++

    @Column(columnDefinition = "TEXT")
    private String code;
}
