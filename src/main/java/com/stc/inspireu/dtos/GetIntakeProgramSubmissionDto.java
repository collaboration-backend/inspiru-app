package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetIntakeProgramSubmissionDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private Long intakeProgramId;

    private String intakeProgramName;

    private String phase;

    private String email;

    private String startupName;

    private String jsonRegistrationForm;

    private String jsonAssessmentEvaluationForm;

    private String jsonScreeningEvaluationForm;

    private String jsonBootcampEvaluationForm;

    private String jsonProgressReport;

    private String jsonProfileCard;

    private String profileInfoJson;

    private Long createdAt;

    private Long interviewStart;

    private Long interviewEnd;

    private Boolean isAbsent;

    private Long interviewStartBootcamp;

    private Long interviewEndBootcamp;

    private Boolean isAbsentBootcamp;

    private Boolean hasStartedScreeningEvaluation = Boolean.FALSE;

    private Boolean hasStartedScreeningEvaluationByMe = Boolean.FALSE;

    private static final String[] CHARACTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q"};

}
