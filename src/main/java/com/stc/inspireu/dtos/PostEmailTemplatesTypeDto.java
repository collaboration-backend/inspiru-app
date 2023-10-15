package com.stc.inspireu.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PostEmailTemplatesTypeDto implements Serializable {

    @ApiModelProperty("EmailTemplate Type Name")
    private String name;

    @ApiModelProperty("EmailTemplate Type Status")
    private String status;

}
