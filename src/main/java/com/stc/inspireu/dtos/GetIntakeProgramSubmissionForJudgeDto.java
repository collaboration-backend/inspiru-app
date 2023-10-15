package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetIntakeProgramSubmissionForJudgeDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private Long intakeProgramId;

    private String intakeProgramName;

    private String phase;

    private String email;

    private String startupName;

    private String profileInfoJson;

    private Long interviewStart;

    private Long interviewEnd;

    private Boolean isAbsent;

    private Long interviewStartBootcamp;

    private Long interviewEndBootcamp;

    private Boolean isAbsentBootcamp;

    private String submissionJson;

}
