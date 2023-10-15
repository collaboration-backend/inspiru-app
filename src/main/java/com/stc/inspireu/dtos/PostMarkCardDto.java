package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class PostMarkCardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "required")
    private String markCardName;

    @NotNull(message = "required")
    private Long intakeProgramId;

    @NotNull(message = "required")
    private String jsonForm;

    private String description;

    private List<MarkCardAcademyRoomDto> markCardDetail;
}
