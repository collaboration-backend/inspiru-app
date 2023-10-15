package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class GetCalendarEventDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotBlank(message = "month required")
    private Integer month;

    @NotBlank(message = "year required")
    private Integer year;

    @NotBlank(message = "timeZone required")
    private String timeZone;
}
