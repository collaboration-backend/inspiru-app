package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class MarkCardStartupDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long intakeProgramId;

    private Long startupId;

    private String startupName;

    private Long updatedUserId;

    private String updatedUserName;

    private Long updatedAt;

    private String status;

    private Float amountPaid;

    private String startupProfileInfoJson;

    public MarkCardStartupDto(Long intakeProgramId, Long startupId, String startupName, String startupProfileInfoJson,
                              Long updatedUserId, String updatedUserName, Long updatedAt, String status, Float amountPaid) {
        super();
        this.intakeProgramId = intakeProgramId;
        this.startupId = startupId;
        this.startupName = startupName;
        this.startupProfileInfoJson = startupProfileInfoJson;
        this.updatedUserId = updatedUserId;
        this.updatedUserName = updatedUserName;
        this.updatedAt = updatedAt;
        this.status = status;
        this.amountPaid = amountPaid;
    }

}
