package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttendanceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long createdAt;

    private Long updatedAt;

    private String memberName;

    private String startupName;

    private String sessionName;

    private String attendance;

    private Long attendanceDate;

}
