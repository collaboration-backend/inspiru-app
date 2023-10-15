package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import java.io.Serializable;

@Data
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Email(message = "must be valid email")
    private String email;

    private String alias;

    private String invitationStatus;

    private Long roleId;

    private String roleName;

    private String phoneNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private Long startupId;

    private long createdAt;

    private long updatedAt;

    private Boolean isRemovable;
}
