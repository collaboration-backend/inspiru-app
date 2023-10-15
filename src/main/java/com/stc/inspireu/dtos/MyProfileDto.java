package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class MyProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "startupName required")
    private String startupName;

    @NotBlank(message = "memberName required")
    private String memberName;

    @NotBlank(message = "phoneNumber required")
    private String phoneNumber;

    @NotBlank(message = "phoneDialCode required")
    private String phoneDialCode;

    @NotBlank(message = "phoneCountryCodeIso2 required")
    private String phoneCountryCodeIso2;

    @NotBlank(message = "programIncubating required")
    private String programIncubating;

    @Email(message = "must be valid email")
    @NotBlank(message = "registratedEmailAddress required")
    private String registratedEmailAddress;
}
