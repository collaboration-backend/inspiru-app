package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class ProgressReportPostRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "month required")
    private Integer month;

    @NotNull(message = "year required")
    private Integer Year;

    @NotNull(message = "isSubmitted required")
    private Boolean isSubmitted;

    @NotBlank(message = "reportName required")
    private String reportName;

    @NotBlank(message = "jsonProgressReportDetail required")
    private String jsonProgressReportDetail;

    private List<ProgressReportFilePostRequestDto> jsonReportFiles;
}
