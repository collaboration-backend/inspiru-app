package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
public class AttendanceEventDetailDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String memberName;

    private Long memberID;

    private String startupName;

    private String sessionName;

    private String attendance;

    private Long attendanceLongDate;

    private Date attendanceDate;

    private Long startupId;

    private Map<String, Object> slot;

    private Map<String, Object> oneToOneMeeting;

    private Map<String, Object> trainingSession;

}
