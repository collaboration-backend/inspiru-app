package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostAttendanceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "memberId required")
    private Long memberId;

    @NotNull(message = "isPresent required")
    private Boolean isPresent;

    @NotNull(message = "isLate required")
    private Boolean isLate;
}
