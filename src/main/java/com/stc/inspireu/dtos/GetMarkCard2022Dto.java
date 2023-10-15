package com.stc.inspireu.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class GetMarkCard2022Dto {

    private Long markCardId;

    private String name;

    private LocalDateTime createdAt;

    private String intakeProgramName;

    private Long intakeProgramId;

    private Date startDate;

    private Date endDate;

}
