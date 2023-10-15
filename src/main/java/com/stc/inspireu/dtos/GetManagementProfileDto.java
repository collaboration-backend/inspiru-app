package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetManagementProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String profilePic;

    private String memberName;

    private String phoneNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private String registratedEmailAddress;

    private String signaturePic;

}
