package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class BeneficiaryApplicationsDTO {

    private Long id;

    private String name;

    private String email;

    private String submittedDate;

    private String intake;

    private String status;
}
