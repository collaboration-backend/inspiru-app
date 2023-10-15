package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class StartupCompanyProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private MultipartFile companyPic;

    @NotBlank(message = "startupName required")
    private String startupName;

    @NotBlank(message = "companyProfile required")
    private String companyProfile;

    private String companyDescription;

    private String revenueModel;

    private String segment;

    private String status;

    private String competitor;

    private String profileCardJsonForm;

    private String registartionJsonForm;

    private String profileInfoJson;

}
