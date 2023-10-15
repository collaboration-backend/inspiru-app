package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class WorkshopSessionSubmissionManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String submittedFileName;

    private String submittedfileType;

    private String startUpName;

    private Long submittedOn;

    private String submittedBy;

    private String status;

    private Boolean isLate;

    private Long metaDataId;

    private Long startupId;

    private Long metaDataParentId;

    private String startupProfileInfoJson;

    private Long intakeProgramId;
}
