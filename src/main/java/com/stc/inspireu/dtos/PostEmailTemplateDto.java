package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostEmailTemplateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String templateName;

    private String subject;

    private String header;

    private String content;

    private String footer;

    private Long intakeNumber;
}
