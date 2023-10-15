package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String tag;

    private String versionId;

    private Long userId;

    private Long intakeProgramId;

    private String userName;

    private String inTakePgmName;

    private String status;

    private long createdAt;

    private long updatedAt;


}
