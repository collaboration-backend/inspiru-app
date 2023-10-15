package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "evaluation_judge_summaries", indexes = {
    @Index(name = "judge_evaluation", columnList = "submittedJudgeId, evaluationSummaryId", unique = true)})
public class EvaluationJudgeSummary extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String screeningEvaluationJsonForm;

    @Column(columnDefinition = "TEXT")
    private String jsonForm;

    @Column(columnDefinition = "TEXT")
    private String jsonFormBootcamp;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "evaluationSummaryId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EvaluationSummary evaluationSummary;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "submittedJudgeId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User submittedJudge;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "intakeProgramSubmissionId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgramSubmission intakeProgramSubmission;
}
