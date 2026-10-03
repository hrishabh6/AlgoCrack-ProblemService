package com.hrishabh.problemservice.dailychallenge.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "daily_challenge_audit")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChallengeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(name = "challenge_date", nullable = false)
    private LocalDate challengeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DailyChallengeAuditAction action;

    @Column(name = "old_question_id")
    private Long oldQuestionId;

    @Column(name = "new_question_id")
    private Long newQuestionId;

    private String actor;

    @Column(length = 512)
    private String reason;

    @Column(name = "request_id", length = 64)
    private String requestId;
}
