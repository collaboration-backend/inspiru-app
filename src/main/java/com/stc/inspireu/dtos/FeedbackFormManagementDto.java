package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeedbackFormManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String status;

    private String createdBy;

    private String startupName;

    private Long startupId;

    private String workshopSessionName;

    private String jsonForm;

    private Long createdAt;

    private Long updatedAt;

    private Long noOfSubmissions;

}
