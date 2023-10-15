package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrainingMaterialManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String materialName;

    private String materialFile;

    private String description;

    private String createdBy;

    private String status;
}
