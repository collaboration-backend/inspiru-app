package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class GetAssessmentEvaluationFormDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String formName;

    private Long FormId;

    private String status;

    private String description;

    private String jsonForm;

    private LocalDateTime createdAt;

    private String createdUser;

    private String publishedUser;

    private Date publishedAt;

    private String intakeProgram;

}
