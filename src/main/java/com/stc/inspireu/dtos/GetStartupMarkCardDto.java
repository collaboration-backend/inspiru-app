package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetStartupMarkCardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long startupId;

    private String startupName;

    private Long markCardId;

    private String markCardName;

    private Long modifiedAt;

    private String modifiedUser;

    private String status;
}
