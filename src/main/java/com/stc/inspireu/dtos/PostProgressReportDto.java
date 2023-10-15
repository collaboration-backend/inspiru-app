package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostProgressReportDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull(message = "month required")
    private Integer month;

    @NotNull(message = "year required")
    private Integer year;

    private String reportName;
}
