package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetStartupDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String startupName;

    private Long intakeProgramId;

    private String intakeProgramName;

    private String jobTitle;

    private String companyProfile;

    private String companyPic;

    private String companyDescription;

    private String revenueModel;

    private String segment;

    private String status;

    private String competitor;

    private Long createdAt;

    private Long updatedAt;

    private String profileCardJsonForm;

    private String profileInfoJson;
}
