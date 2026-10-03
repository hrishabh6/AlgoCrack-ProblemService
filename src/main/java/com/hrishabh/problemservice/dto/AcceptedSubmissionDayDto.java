package com.hrishabh.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Accepted submission fact returned by SubmissionService. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptedSubmissionDayDto {
    private Long questionId;
    private LocalDate submissionDate;
}
