package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PostSurveyTemplateManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    @NotBlank(message = "jsonForm required")
    private String jsonForm;

    private Long formTemplateId;

    private Boolean isNew = false;

    private Long dueDate;
}
