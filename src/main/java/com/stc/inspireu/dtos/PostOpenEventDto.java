package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostOpenEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "eventName required")
    private String eventName;

    @NotBlank(message = "eventPhase required")
    private String eventPhase;

    @NotBlank(message = "timezone required")
    private String timezone;

    @NotNull(message = "sessionStart required")
    private Long sessionStart;

    @NotNull(message = "sessionEnd required")
    private Long sessionEnd;

    @NotNull(message = "intakeProgramId required")
    private Long intakeProgramId;

    private String meetingRoom;

    private String meetingLink;

    private String description;

    @NotNull(message = "isActive required")
    private Boolean isActive;

    @NotBlank(message = "schedulingMethod required")
    private String schedulingMethod;

    @NotBlank(message = "jsonEventInfo required")
    private String jsonEventInfo;

}
