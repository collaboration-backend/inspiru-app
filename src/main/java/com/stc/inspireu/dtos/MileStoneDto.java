package com.stc.inspireu.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class MileStoneDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String mileStoneName;
    @NotNull
    private Integer mileStoneNumber;
    @NotNull
    private Long intakePgmId;

    private String inTakePgmName;

    @NotNull
    private String condition;

    @NotNull
    private String applicableTo;

    @ApiModelProperty("not for insertion")
    private String intakePgmName;

    @ApiModelProperty("not for insertion")
    private String createdBy;

    @ApiModelProperty("not for insertion")
    private String status;

}
