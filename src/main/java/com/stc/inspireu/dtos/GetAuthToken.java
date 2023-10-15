package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class GetAuthToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = "must be valid email")
    @NotBlank(message = "email required")
    private String email;

    private Integer otp;

    private String context;

    private Boolean isEmail;

}
