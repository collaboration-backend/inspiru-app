package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

@Data
public class StartupsRegistrationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "startupName required")
    private String startupName;

    @NotBlank(message = "memberName required")
    private String memberName;

    private String phoneDialCode;

    private String iso2CountryCode;

    @NotBlank(message = "phoneNumber required")
    private String phoneNumber;

    @NotBlank(message = "jobTitle required")
    private String jobTitle;

    private String programIncubating;

    private String programName;

    @NotNull(message = "intakeNumber required")
    @Min(1)
    @Max(10000000)
    private Integer intakeNumber;

    @Email(message = "must be valid email")
    @NotBlank(message = "registratedEmailAddress required")
    private String registratedEmailAddress;

    @NotBlank(message = "password required")
    private String password;

    @NotBlank(message = "confirmPassword required")
    private String confirmPassword;

    @NotBlank(message = "inviteToken required")
    private String inviteToken;

}
