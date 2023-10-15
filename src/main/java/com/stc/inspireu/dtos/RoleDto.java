package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String roleName;

    private String roleAlias;

    private String description;

    private Boolean willManagement;

}
