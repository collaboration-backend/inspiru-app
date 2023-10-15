package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostAssignmentManagementDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    @NotNull(message = "dueDate required")
    private Long dueDate;
}
