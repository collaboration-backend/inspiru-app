package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class PostPartnerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String details;

    private String phoneNumber;

    private String phoneDialCode;

    private String phoneCountryCodeIso2;

    private String email;

    private String link;

    private MultipartFile logo;
}
