package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ResetPasswordDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "password required")
    private String password;

    @NotBlank(message = "confirmPassword required")
    private String confirmPassword;

    @NotBlank(message = "resetPasswordToken required")
    private String resetPasswordToken;
}
