package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class
GetMarkCardStartupsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long startupId;

    private Long markCardId;

    private String markCardJson;

    private Long markCardRefId;

    private String startupName;

    private String status;

    private String updatedBy;

    private Long updateDate;

    private Float amount;

    private String academicRoomName;

    private Long academicRoomId;

    private Date academicRoomEndDate;

    private int progressReportPercentage;

    private Date progressReportLastUpdated;

    private Boolean isMarkCardGenerated;

}
