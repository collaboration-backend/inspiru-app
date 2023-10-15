package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PutOne2OneMeetingManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isAccept = false;

    private Boolean isNewTiming = false;

    @NotNull(message = "sessionStart required")
    private Long currentSessionStart;

    @NotNull(message = "sessionEnd required")
    private Long currentSessionEnd;

    private Long proposedSessionStart;

    private Long proposedSessionEnd;

    @NotNull(message = "startupId required")
    private Long startupId;

}
