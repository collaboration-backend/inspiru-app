package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostManagementProgressReportDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull
    private String reportName;

    @NotNull
    private Long intakePgmId;

    @NotNull
    private String jsonForm;
}
