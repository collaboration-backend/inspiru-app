package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAttendanceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long attendanceDate;

    private String day;

    private String Status;

    private Long memberId;

    private String memberName;

    private String memberTitle;

}
