package com.stc.inspireu.dtos;

import com.stc.inspireu.models.AssignmentFile;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetAssignmentFileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String path;

}
