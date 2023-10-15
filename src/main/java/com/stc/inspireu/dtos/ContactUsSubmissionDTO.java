package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ContactUsSubmissionDTO {

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotEmpty(message = "Mobile is required")
    private String mobile;

    @NotEmpty(message = "Message is required")
    private String message;

    @NotNull(message = "Subject is required")
    private Long subject;
}
