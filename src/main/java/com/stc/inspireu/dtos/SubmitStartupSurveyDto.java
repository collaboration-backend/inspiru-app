package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SubmitStartupSurveyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "jsonForm required")
    private String jsonForm;

}
