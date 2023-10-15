package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class GetIntakeProgramSubmissionForJudgeAppDto {

    private Long id;

    private String email;

    private String startupName;

    private String profileInfoJson;

    private Boolean isAbsent;

    private Boolean isAbsentBootcamp;

    private String phase;

}
