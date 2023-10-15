package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class MarkAttendanceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;

    private Long eventTypeId;

    private Long startupId;

    private String userAttendanceJson;
}
