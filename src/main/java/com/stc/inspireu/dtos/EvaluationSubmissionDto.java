package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EvaluationSubmissionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "intakeProgramId required")
    private Long intakeProgramId;

    @NotNull(message = "submissionId required")
    private Long submissionId;

    @NotBlank(message = "jsonForm required")
    private String jsonForm;

    @NotBlank(message = "jsonFormValues required")
    private String jsonFormValues;

    @NotBlank(message = "phase required")
    private String phase;

    @Email(message = "must be valid email")
    private String email;

    private String startupName;

    private String profileCardForm;
}
