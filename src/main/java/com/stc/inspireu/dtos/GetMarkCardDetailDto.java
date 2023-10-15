package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetMarkCardDetailDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long academyRoomId;

    private String jsonForm;
}
