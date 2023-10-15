package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class InviteStartupMemberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = "must be valid email")
    @NotBlank(message = "email required")
    private String email;

    @NotNull(message = "willAdmin required")
    private Boolean willAdmin;

}
