package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetWorkshopSessionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String name;

    private String description;

    private Integer percentageFinish;

    private Long academyRoomId;

    private Long refAcademyRoomId;

    private Long sessionStart;

    private Long sessionEnd;
}
