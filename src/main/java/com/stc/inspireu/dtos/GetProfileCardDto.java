package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class GetProfileCardDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long profileCardId;

    private String ProfileCardName;

    private String status;

    private String createdUser;

    private String startup;

    private String jsonForm;

    private String refProfileCard;

    private LocalDateTime createdAt;

    private Date updatedAt;
}
