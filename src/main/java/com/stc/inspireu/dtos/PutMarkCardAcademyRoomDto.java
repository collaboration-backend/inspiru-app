package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PutMarkCardAcademyRoomDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "isSubmitted required")
    private Boolean isSubmitted;

    @NotNull(message = "required")
    private String jsonForm;

    @NotNull(message = "required")
    private Integer totalPayment;

    @NotNull(message = "required")
    private Integer allottedPayment;
}
