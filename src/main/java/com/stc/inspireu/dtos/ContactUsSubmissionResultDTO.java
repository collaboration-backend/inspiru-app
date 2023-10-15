package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class ContactUsSubmissionResultDTO {

    private String name;

    private String email;

    private String mobile;

    private String subject;

    private String message;

    private String createdOn;
}
