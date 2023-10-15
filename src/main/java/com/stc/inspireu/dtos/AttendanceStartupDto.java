package com.stc.inspireu.dtos;

import lombok.Data;

import java.util.List;

@Data
public class AttendanceStartupDto {

    private Long attendanceDate;

    private Long oneToOneMeetingId;

    private Long trainingSessionId;

    private Long membersCount;

    private String name;

    private Long startupId;

    private String startupName;

    private List<UserAttendanceDto> membersAttended;

    private Long markedDate;

}
