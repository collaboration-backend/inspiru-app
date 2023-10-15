package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAttendancePercentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String memberName;

    private String startupName;

    private Float attencendancePercent;

    private Long memberId;

}
