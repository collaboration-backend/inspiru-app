package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostIntakeProgramDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "programName required")
    private String programName;

    @NotNull(message = "periodStart required")
    private Long periodStart;

    @NotNull(message = "periodEnd required")
    private Long periodEnd;

    @NotNull(message = "isDraft required")
    private Boolean isDraft;
}
