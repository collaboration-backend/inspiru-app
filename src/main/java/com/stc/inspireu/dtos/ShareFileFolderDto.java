package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class ShareFileFolderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Long> members;

}
