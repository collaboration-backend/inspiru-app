package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class ProgressReportFilePostRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileInfo;

    private MultipartFile progessReportiles;

}
