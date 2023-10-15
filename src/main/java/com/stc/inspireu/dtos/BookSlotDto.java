package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class BookSlotDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull(message = "sessionStart required")
    private Long sessionStart;

    @NotNull(message = "sessionEnd required")
    private Long sessionEnd;

    @NotBlank(message = "reason required")
    private String reason;

}
