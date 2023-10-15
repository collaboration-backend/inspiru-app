package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "evaluation_summaries")
public class EvaluationSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId", nullable = false)
    private IntakeProgram intakeProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submittedUserId", nullable = false)
    private User submittedUser;

    @Column(nullable = false)
    private String email;

    @Column
    private String phase;

    @Column(name = "screening_evaluators_marks", columnDefinition = "TEXT")
    private String screeningEvaluatorsMarks;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String judgesMarks;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String judgeBootcampMarks;

    @Column
    private Float total;

    @OneToOne(fetch = FetchType.LAZY)
    private IntakeProgramSubmission intakeProgramSubmission;

    @Column
    private Float avarage;

    @Column
    private Float bootcampTotal;

    @Column
    private Float bootcampAvarage;

    @Column
    private Float screeningTotal;

    @Column
    private Float screeningAverage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String profileCard;

    @Column
    private String status;

    @Column
    private String startupName;
}
