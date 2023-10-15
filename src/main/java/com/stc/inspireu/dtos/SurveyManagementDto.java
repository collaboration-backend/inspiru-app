package com.stc.inspireu.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class SurveyManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String status;

    private String createdBy;

    private String startupName;

    private Long startupId;

    private String workshopSessionName;

    private String jsonForm;

    private Long createdOn;

    private Long modifiedOn;

    private Long noOfSubmissions;

    private Long dueDate;

    private Long submitDate;

    private String submittedBy;

    private Long intakeProgramId;

    private String startupProfileInfoJson;


    public SurveyManagementDto(Long id, String name, String status, String createdBy, Date createDate, String jsonForm,
                               Long noOfSubmissions) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.noOfSubmissions = noOfSubmissions;
        this.jsonForm = jsonForm;
        this.createdBy = createdBy != null ? createdBy : null;
        this.createdOn = createDate.toInstant().toEpochMilli();
    }

}
