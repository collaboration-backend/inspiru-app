package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class InviteUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = "must be valid email")
    @NotBlank(message = "registratedEmailAddress required")
    private String registratedEmailAddress;

    @NotNull(message = "roleId required")
    private Long roleId;

    private String programName;

    private Long intakeNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private String phoneNumber;

    private String name;
}
