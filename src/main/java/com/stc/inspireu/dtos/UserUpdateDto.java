package com.stc.inspireu.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(example = "John Doe")
    private String alias;

    @ApiModelProperty(example = "966")
    private String phoneDialCode;

    @ApiModelProperty(example = "SA")
    private String phoneCountryCodeIso2;

    @ApiModelProperty(example = "500007788")
    private String phoneNumber;

    @ApiModelProperty(example = "6")
    private Long roleId;

}
