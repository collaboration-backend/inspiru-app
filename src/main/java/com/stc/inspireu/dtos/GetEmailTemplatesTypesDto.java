package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetEmailTemplatesTypesDto implements Serializable {

    private Long EmailTemplateId;

    private String name;

    private String status;

    private String createdUser;

    private Long createdAt;

    private String iconName;

}
