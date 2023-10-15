package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class BeneficiarySignupDTO {

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email address", regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    private String email;

    @NotEmpty(message = "Mobile is required")
    private String mobile;

    @NotEmpty(message = "Password is required")
    private String password;

    private String phoneCountryCodeIso2;
}
