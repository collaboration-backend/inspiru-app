package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostAttendancePercentageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long intakeProgramId;

    private Long startupId;

    private Integer percentage;

    private Long academyRoomId;

    private Long workshopSessionId;

}
