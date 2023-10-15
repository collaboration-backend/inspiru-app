package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetFeedbackDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String uploadedBy;

    private String status;

    private String jsonForm;

}
