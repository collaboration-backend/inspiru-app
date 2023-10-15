package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostTrainingSessionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "meetingName required")
    private String meetingName;

    @NotNull(message = "willOnline required")
    private Boolean willOnline;

    @NotNull(message = "sessionStartDate required")
    private Long sessionStartDate;

    @NotNull(message = "sessionEndDate required")
    private Long sessionEndDate;

    @NotBlank(message = "sessionStartTime required")
    private String sessionStartTime;

    @NotBlank(message = "sessionEndTime required")
    private String sessionEndTime;

    private String description;

    @NotBlank(message = "meetingRoomOrLink required")
    private String meetingRoomOrLink;

    @NotNull(message = "intakeProgramId required")
    private Long intakeProgramId;

    @NotNull(message = "academyRoomId required")
    private Long academyRoomId;

    @NotNull(message = "workshopSessionId required")
    private Long workshopSessionId;

    private Boolean isRecurring;
}
