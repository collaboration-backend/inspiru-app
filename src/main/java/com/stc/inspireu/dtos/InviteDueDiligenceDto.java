package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class InviteDueDiligenceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "startupId required")
    private Long startupId;

    @NotNull(message = "dueDiligenceId required")
    private Long dueDiligenceId;

    @Email(message = "must be valid email")
    @NotBlank(message = "email required")
    private String email;
}
