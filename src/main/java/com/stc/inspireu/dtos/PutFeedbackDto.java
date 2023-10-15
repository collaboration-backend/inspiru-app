package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PutFeedbackDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "jsonForm required")
    private String jsonForm;

    @NotNull(message = "submitStatus required")
    private Boolean submitStatus;

    @NotNull(message = "workshopSessionId required")
    private Long workshopSessionId;

    @NotNull(message = "startupId required")
    private Long startupId;
}
