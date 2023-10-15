package com.stc.inspireu.dtos;

import com.stc.inspireu.models.Startup;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class GetUserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    private String alias;

    private String invitationStatus;

    private Long roleId;

    private String roleAlias;

    private String roleName;

    private String phoneNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private Long startupId;

    private String startupName;

    private Long createdAt;

    private Long intakeId;

    private String intakeName;

    public GetUserDto(Long id, String email, String alias, String invitationStatus, String phoneNumber,
                      String phoneDialCode, String phoneCountryCodeIso2, LocalDateTime createdAt, Startup startup) {
        super();
        this.id = id;
        this.email = email;
        this.alias = alias;
        this.invitationStatus = invitationStatus;
        this.phoneNumber = phoneNumber;
        this.phoneDialCode = phoneDialCode;
        this.phoneCountryCodeIso2 = phoneCountryCodeIso2;
        if (startup != null) {
            this.startupId = startup.getId();
            this.startupName = startup.getStartupName();
        }
        this.createdAt = createdAt != null ? ZonedDateTime.of(createdAt, ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
    }
}
