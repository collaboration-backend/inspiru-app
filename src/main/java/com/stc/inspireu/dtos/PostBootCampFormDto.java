package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostBootCampFormDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String formName;

    private String status;

    private String description;

    @NotNull
    private String jsonForm;

    private Long intakeProgramId;

    @NotNull
    private String evaluationPhase;

}
