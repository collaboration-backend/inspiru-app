package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostDueDiligenceTemplate2021Dto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "intakeProgramId required")
    private Long intakeProgramId;

    @NotNull(message = "jsonForm required")
    private String jsonForm;
}
