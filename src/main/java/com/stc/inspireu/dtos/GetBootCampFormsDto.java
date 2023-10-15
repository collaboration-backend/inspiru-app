package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class GetBootCampFormsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String formName;

    private String status;

    private String description;

    private String jsonForm;

    private LocalDateTime createdAt;

    private String createdUser;

    private String publishedUser;

    private Date publishedAt;

    private String intakeProgram;

    private String evaluationPhase;
}
