package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetOpenEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String eventName;

    private String eventPhase;

    private Long createdAt;

    private Long updatedAt;

    private Long sessionStart;

    private Long sessionEnd;

    private Long intakeProgramId;

    private Long createdUserId;

    private String createdUserName;

    private String meetingRoom;

    private String meetingLink;

    private String meetingHostLink;

    private String description;

    private Boolean isActive;

    private String schedulingMethod;

    private String jsonEventInfo;

}
