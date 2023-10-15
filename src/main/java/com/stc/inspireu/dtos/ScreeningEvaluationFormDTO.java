package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ScreeningEvaluationFormDTO implements Serializable {

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
