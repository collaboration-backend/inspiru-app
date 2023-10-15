package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class DueDiligenceNote2021Dto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String userName;

    private boolean isManagement;

    private String note;

    private long createdAt;

    private long updatedAt;
}
