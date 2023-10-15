package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class RegistrationFormDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean bannerDeleted;

    private String registrationFormName;

    private Long intakePgmId;

    private Long dueDate;

    private String formJson;

    private String language;

    private String description;

    private MultipartFile banner;

    private Long copiedFromId;
}
