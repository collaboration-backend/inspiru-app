package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class ContactUsSubjectDTO {

    @NotEmpty(message = "Subject is required")
    @Size(min = 2, max = 200, message = "Subject should be between 2-200 characters")
    private String subject;

    @NotEmpty(message = "Emails are required")
    private String emails;
}
