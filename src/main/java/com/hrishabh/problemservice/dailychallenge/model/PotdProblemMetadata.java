package com.hrishabh.problemservice.dailychallenge.model;

import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Table(name = "potd_problem_metadata")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotdProblemMetadata {

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean eligible = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "curation_status", nullable = false, length = 20)
    @Builder.Default
    private PotdCurationStatus curationStatus = PotdCurationStatus.NEEDS_REVIEW;

    @Column(name = "quality_score", nullable = false)
    @Builder.Default
    private int qualityScore = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_tag_id")
    private Tag primaryTag;

    @Column(name = "family_key", length = 128)
    private String familyKey;

    @Column(name = "cooldown_days_override")
    private Integer cooldownDaysOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 20)
    @Builder.Default
    private PotdValidationStatus validationStatus = PotdValidationStatus.NOT_VALIDATED;

    @Column(name = "validated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validatedAt;

    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;
}
