package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Data
public class BootCampSelectedStartupDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "require atleat one startupRegisterationId")
    private Set<@NotNull Long> startupRegisterationIds;

}
