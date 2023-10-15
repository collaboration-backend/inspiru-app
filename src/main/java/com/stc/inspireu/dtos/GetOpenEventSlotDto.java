package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetOpenEventSlotDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long day;

    private Short startTimeHour;

    private Short startTimeMinute;

    private Short endTimeHour;

    private Short endTimeMinute;

    private Long openEventId;

    private String email;
}
