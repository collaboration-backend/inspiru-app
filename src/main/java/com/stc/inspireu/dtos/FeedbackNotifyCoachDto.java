package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeedbackNotifyCoachDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long coachId;

    private String startupName;

    private Long startupId;
}
