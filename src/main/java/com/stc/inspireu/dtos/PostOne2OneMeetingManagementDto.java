package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostOne2OneMeetingManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "meetingName required")
    private String meetingName;

    @NotNull(message = "willOnline required")
    private Boolean willOnline;

    @NotNull(message = "sessionStart required")
    private Long sessionStart;

    @NotNull(message = "sessionEnd required")
    private Long sessionEnd;

    @NotNull(message = "startupId required")
    private Long startupId;

    private String description;

    private String meetingRoom;
}
