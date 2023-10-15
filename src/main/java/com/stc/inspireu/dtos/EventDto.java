package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class EventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String description;

    private Long sessionStart;

    private Long sessionEnd;

}
