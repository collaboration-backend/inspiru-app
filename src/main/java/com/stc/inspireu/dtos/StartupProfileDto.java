package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class StartupProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private MultipartFile profilePic;

    private String startupName;

    private String memberName;

    @NotBlank(message = "phoneNumber required")
    private String phoneNumber;

    @NotBlank(message = "phoneDialCode required")
    private String phoneDialCode;

    @NotBlank(message = "phoneCountryCodeIso2 required")
    private String phoneCountryCodeIso2;

    @NotBlank(message = "programIncubating required")
    private String programIncubating;

    private String registratedEmailAddress;

}
