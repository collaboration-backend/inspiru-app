package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostSurveyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "name required")
    private String name;

    @NotNull(message = "dueDate required")
    private Long dueDate;

    @NotNull(message = "workshopSessionId required")
    private Long workshopSessionId;

    @NotNull(message = "jsonForm required")
    private String jsonForm;

}
