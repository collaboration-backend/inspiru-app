package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ManagementRegistrationDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    @NotBlank(message = "phoneNumber required")
    private String phoneNumber;

    @Email(message = "must be valid email")
    @NotBlank(message = "registratedEmailAddress required")
    private String registratedEmailAddress;

    @NotBlank(message = "jobTitle required")
    private String jobTitle;

    private Long intakeNumber;

    @NotBlank(message = "password required")
    private String password;

    @NotBlank(message = "confirmPassword required")
    private String confirmPassword;

    @NotBlank(message = "inviteToken required")
    private String inviteToken;

    private String phoneDialCode;

    private String iso2CountryCode;

}
