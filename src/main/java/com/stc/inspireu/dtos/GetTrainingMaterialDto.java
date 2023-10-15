package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetTrainingMaterialDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String path;

    private String description;

    private String createdUserName;

    private Long createdAt;

    private Long updatedAt;
}
