package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostMarkCardSummaryDto implements Serializable {

    private Long startupId;

    @NotNull
    private Long academyRoomId;

    private String jsonMarkCards;

    private Long refMarkCardId;

    private Float total;

    private Long intakeProgramId;

    private int progressReportPercentage;
}
