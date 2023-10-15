package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "intake_programs")
public class IntakeProgram extends BaseEntity {

    @Column(nullable = false)
    private String programName;

    @Column(nullable = false)
    private Date periodStart;

    @Column(nullable = false)
    private Date periodEnd;

    @Column(nullable = false)
    private String status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "registrationFormId")
    private RegistrationForm registrationForm;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "assessmentEvaluationFormId")
    private AssessmentEvaluationForm assessmentEvaluationForm;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "bootcampEvaluationFormId")
    private BootcampEvaluationForm bootcampEvaluationForm;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "progressReportId")
    private ProgressReport progressReport;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profileCardId")
    private ProfileCard profileCard;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ScreeningEvaluationForm screeningEvaluationForm;

    @Column
    private Boolean isArchive;

    @Column
    private Boolean bootcampFinished;

    @Column
    private Boolean assessmentFinished;

    @Column(columnDefinition = "TEXT")
    private String formToken;

    @Column
    private int phaseStatus;
}
