package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class One2OneMeetingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String meetingName;

    private Boolean willOnline;

    private Long sessionStart;

    private Long sessionEnd;

    private Long trainerId;

    private String trainerName;

    private String description;
}
