package com.stc.inspireu.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class RegistrationLinkDTO {

    private String intakeRef;

    @JsonFormat(pattern = "dd MMM yyyy")
    private Date dueDate;

    private String registrationLink;


}
