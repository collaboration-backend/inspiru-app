package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OpenEventBookingDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull(message = "day required")
    private Long day;

    @NotNull(message = "startTimeHour required")
    private Short startTimeHour;

    @NotNull(message = "startTimeMinute required")
    private Short startTimeMinute;

    @NotNull(message = "endTimeHour required")
    private Short endTimeHour;

    @NotNull(message = "endTimeMinute required")
    private Short endTimeMinute;

    @NotNull(message = "openEventId required")
    private Long openEventId;

    @NotBlank(message = "email required")
    private String email;

    @NotNull(message = "startDatetime required")
    private Long startDatetime;

    @NotNull(message = "endDatetime required")
    private Long endDatetime;

}
