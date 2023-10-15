package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EditStartupMemberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String alias;

    @Email(message = "must be valid email")
    @NotBlank(message = "email required")
    private String email;

    @NotNull(message = "roleId required")
    private Long roleId;

    @NotBlank(message = "phoneNumber required")
    private String phoneNumber;

    @NotBlank(message = "phoneDialCode required")
    private String phoneDialCode;

    @NotBlank(message = "phoneCountryCodeIso2 required")
    private String phoneCountryCodeIso2;

}
