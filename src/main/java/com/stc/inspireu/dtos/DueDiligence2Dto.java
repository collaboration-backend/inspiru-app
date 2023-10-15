package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DueDiligence2Dto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String templateName;

    private Long intakePgmId;
    private String intakePgmName;

    private String fileType;

    private String path;

    private String status;

    private String publishStatus;

    private Date publishedDate;
    private String createdBy;
    private String publishedBy;

}
