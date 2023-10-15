package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostAcademyRoomDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    @NotBlank(message = "description required")
    private String description;

    private String programName;

    @NotNull(message = "intakeProgramId required")
    private Long intakeProgramId;

    private Long sessionStart;

    private Long sessionEnd;
}
