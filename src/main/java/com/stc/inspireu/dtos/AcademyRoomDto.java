package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class AcademyRoomDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String name;

    private String status;

    private String description;

    private String createdUserName;

    private Boolean markCardNotified;
}
