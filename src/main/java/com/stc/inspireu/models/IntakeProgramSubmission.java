package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "intake_program_submissions")
public class IntakeProgramSubmission extends BaseEntity {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId", nullable = false)
    private IntakeProgram intakeProgram;

    @Column(nullable = false)
    private String phase;

    @Column(nullable = false)
    private String email;

    @Column
    private String startupName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String jsonRegistrationForm;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String jsonAssessmentEvaluationForm;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String jsonBootcampEvaluationForm;

    @Column(columnDefinition = "TEXT")
    private String jsonScreeningEvaluationForm;

    @Column(columnDefinition = "TEXT")
    private String jsonProgressReport;

    @Column(columnDefinition = "TEXT")
    private String jsonProfileCard;

    @Column(columnDefinition = "TEXT")
    private String profileInfoJson;

    @Column
    private Date evaluationStartedOn;

    @Column
    private Date evaluationEndedOn;

    @Column(name = "screening_evaluation_started_on")
    private Date screeningEvaluationStartedOn;

    @Column(name = "screening_evaluation_ended_on")
    private Date screeningEvaluationEndedOn;

    @Column(name = "bootcamp_evaluation_started_on")
    private Date bootcampEvaluationStartedOn;

    @Column(name = "bootcamp_evaluation_ended_on")
    private Date bootcampEvaluationEndedOn;

    @Column
    private String language;

    private Long beneficiaryId;

    @OneToOne(mappedBy = "intakeProgramSubmission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EvaluationSummary evaluationSummary;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "assessment_judges", joinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "intakeProgramSubmissionId")}, inverseJoinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "userId")})
    private Set<User> users;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "bootcamp_judges", joinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "intakeProgramSubmissionId")}, inverseJoinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "userId")})
    private Set<User> bootcampUsers;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "screening_evaluators", joinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "application_id")
    }, inverseJoinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "evaluator_user_id")
    })
    private Set<User> screeningEvaluators = new HashSet<>();

    @Column
    private Date interviewStart;

    @Column
    private Date interviewEnd;

    @Column
    private Boolean isAbsent;

    @Column
    private Date interviewStartBootcamp;

    @Column
    private Date interviewEndBootcamp;

    @Column
    private Boolean isAbsentBootcamp;
}
