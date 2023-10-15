package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PutMarkCardShareDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull(message = "shareMemberId required")
    private Long shareMemberId;

}
