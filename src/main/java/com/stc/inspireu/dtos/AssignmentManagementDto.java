package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AssignmentManagementDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    private String status;

    private Long dueDate;

    private Long reviwed1On;

    private Long reviwed2On;

    private String createdBy;

    private Long noOfSubmissions;

    private String review1Status;

    private String review2Status;

    private String review1By;

    private String review2By;

    private Long submitDate;

    private Long createDate;

    private String submittedBy;

    private List<GetAssignmentFileDto> assignmentFiles;

    private Long startupId;

    private Long intakeProgramId;

    private String startupProfileInfoJson;

    private String startupName;

    private String review1Comment;

    private String review2Comment;

    private Long review1Id;

    private Long review2Id;
}
