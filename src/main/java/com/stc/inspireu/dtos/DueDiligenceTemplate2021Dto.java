package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class DueDiligenceTemplate2021Dto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private boolean isActive;

    private String status;

    private String jsonForm;

    private String intakeProgram;

    private long startedOn;

    private long endBy;

    private String startupName;

    private Long startupId;

    private long createdAt;

    private long updatedAt;

    private String createdBy;

    private String submittedBy;

    private String publishedBy;

    private long submittedOn;

    private long reviewedOn;

    private String reviewedBy;

    private boolean isArchive;

    private long intakeProgramId;

    private String jsonFieldActions;

    private String invitationStatus;

    private Long userId;

}
