package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetStartupProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String profilePic;

    private String startupName;

    private String memberName;

    private String phoneNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private String programIncubating;

    private String registratedEmailAddress;

    private Long intakeProgramId;

}
