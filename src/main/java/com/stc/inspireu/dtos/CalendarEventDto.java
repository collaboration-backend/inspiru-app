package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class CalendarEventDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private Long startupId;

    private String startupName;

    private Map<String, Object> slot;

    private Map<String, Object> trainingSession;

    private Map<String, Object> oneToOneMeeting;

    private Long sessionStart;

    private Long sessionEnd;

    private Long eventDate;

}
