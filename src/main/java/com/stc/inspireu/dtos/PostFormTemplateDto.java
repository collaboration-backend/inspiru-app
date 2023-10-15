package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

@Data
public class PostFormTemplateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "formType required")
    private String formType;

    @NotBlank(message = "formName required")
    private String formName;

    @NotBlank(message = "jsonForm required")
    private String jsonForm;

    private Date lastAcceptingDate;

}
