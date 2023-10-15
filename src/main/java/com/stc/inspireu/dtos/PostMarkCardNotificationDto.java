package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostMarkCardNotificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    private String jsonMarkCards;

    private Long progressReportId;

    private Float payableAmount;
}
