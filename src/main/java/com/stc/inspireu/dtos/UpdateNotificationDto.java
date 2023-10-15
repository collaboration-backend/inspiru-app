package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UpdateNotificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "enableEmail required")
    private Boolean enableEmail;

    @NotNull(message = "enableWeb required")
    private Boolean enableWeb;
}
