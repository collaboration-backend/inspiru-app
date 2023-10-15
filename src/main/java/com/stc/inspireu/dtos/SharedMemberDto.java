package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class SharedMemberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String roleName;

    private Boolean sharedStatus;
}
