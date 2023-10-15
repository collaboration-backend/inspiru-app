package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

@Data
public class ShareIntakeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(min = 1, message = "at least one id required")
    private Set<Long> sharedMemberIds;

}
