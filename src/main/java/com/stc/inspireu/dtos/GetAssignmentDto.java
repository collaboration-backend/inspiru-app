package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetAssignmentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    private String status;

    private Long dueDate;

    private Long reviwed1On;

    private String createdUser;

    private Long submittedOn;

    private Long reviwed2On;

    private String reviwed1Status;

    private String reviwed2Status;

}
