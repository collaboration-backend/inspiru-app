package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PostAcademyRoomWorkShopSessionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    private String description;

    private Long sessionStart;

    private Long sessionEnd;

    private Boolean willOnline;

    private String meetingRoomOrLink;


}
