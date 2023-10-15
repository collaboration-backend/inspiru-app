package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProgressReportDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long intakeProgramId;

    private Long progressReportId;

    private Integer month;

    private Integer Year;

    private String reportName;

    private String status;

    private String createdUserName;

    private String submittedUserName;

    private long submittedDate;

    private long createdDate;

    private String jsonReportDetail;

    private String jsonForm;

    private Long startupId;

    private String startupProfileInfoJson;

    private List<ProgressReportFileDto> reportFiles;

    private Long refProgressReportId;
}
