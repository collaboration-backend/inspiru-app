package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetJudgeCalenderEventsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private Long sessionStart;

    private Long sessionEnd;

    private String phase;

    private Long eventDate;

    private Long intakeProgramId;

    private String intakeProgramName;

    private String email;

    private Long JudgeId;

    private String JudgeName;

}
