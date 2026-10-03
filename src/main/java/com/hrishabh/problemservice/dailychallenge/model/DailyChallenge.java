package com.hrishabh.problemservice.dailychallenge.model;

import com.hrishabh.problemservice.models.BaseModel;
import com.hrishabh.problemservice.models.Question;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "daily_challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChallenge extends BaseModel {

    @Column(name = "challenge_date", nullable = false, unique = true)
    private LocalDate challengeDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DailyChallengeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 20)
    private DailyChallengeSelectionType selectionType;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(name = "scheduler_version", nullable = false, length = 64)
    private String schedulerVersion;

    @Column(name = "configuration_hash", nullable = false, length = 64)
    private String configurationHash;

    @Column(name = "deterministic_seed", nullable = false, length = 128)
    private String deterministicSeed;

    @Column(name = "selection_score", precision = 10, scale = 6)
    private BigDecimal selectionScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selection_reason", columnDefinition = "json")
    private String selectionReason;

    @Column(name = "published_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedAt;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private long version = 0L;
}
