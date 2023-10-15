package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetIntakeProgramDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String programName;

    private Long createdAt;

    private Long periodStart;

    private Long periodEnd;

    private String status;

    private Long registrationFormId;

    private Long assessmentEvaluationFormId;

    private Long bootcampEvaluationFormId;

    private String registrationFormName;

    private String assessmentEvaluationFormName;

    private String bootcampEvaluationFormName;

    private String registrationFormJsonForm;

    private String assessmentEvaluationFormJsonForm;

    private String bootcampEvaluationFormJsonForm;

    private Boolean bootcampFinished;

    private Boolean assessmentFinished;

    private Long profileCardFormId;

    private String profileCardFormName;

    private String profileCardJsonForm;

}
