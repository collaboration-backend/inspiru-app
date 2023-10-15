package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class GetRegistrationFormDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String formName;

    private String status;

    private String banner;

    private String publishedUser;

    private String jsonForm;

    private Date dueDate;

    private String periodEnd;

    private LocalDateTime createdDate;

    private String createdUser;

    private Long intakeProgramId;

    private String intakeProgramName;

    private Date publishedDate;

    private Long formId;

    private String language;

    private String description;

}
