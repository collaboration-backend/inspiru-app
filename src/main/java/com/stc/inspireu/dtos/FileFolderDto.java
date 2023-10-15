package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileFolderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String tags;

    private Boolean isFile;

    private Boolean isPublic;

    private String description;

    private String parentFolder;

    private Long createdAt;

    private Long updatedAt;

    private Long createdUserId;

    private String createdUserName;

    private Long intakeProgramId;

    private String intakeProgramName;

    private Long refFileFolderId;

    private String refFileFolderName;

    private String uid;

}
