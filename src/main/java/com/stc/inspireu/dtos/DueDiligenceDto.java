package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class DueDiligenceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dueDiligenceFileTypeId;

    private String dueDiligenceFileType;

    private String status;
}
