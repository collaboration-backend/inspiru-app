package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PutSurveyManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "status required")
    private String status;

    @NotBlank(message = "comments required")
    private String comments;

    private Long dueDate;
}
