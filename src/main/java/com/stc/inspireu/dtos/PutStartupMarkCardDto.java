package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class PutStartupMarkCardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "isSubmitted required")
    private Boolean isSubmitted;

    @NotNull(message = "required")
    private List<MarkCardAcademyRoomDto> markCardDetail;
}
