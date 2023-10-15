package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class EvaluationSummaryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private Long intakeProgramId;

    private String intakeProgramName;

    private Long submittedUserId;

    private String submittedUserName;

    private String email;

    private String phase;

    private String judgesMarks;

    private Float total;

    private Float avarage;

    private Float screeningTotal;

    private Float screeningAverage;

    private String profileCard;

    private String judgeBootcampMarks;

    private String screeningEvaluatorsMarks;

    private Float bootcampTotal;

    private Float bootcampAvarage;

    private String startupName;

    private Long applicationId;
}
