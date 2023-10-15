package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostFileSettingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "fileTypes required")
    private String fileTypes;

    @Min(value = 1, message = "minimum fileSize 1MB")
    @Max(value = 10, message = "maximum fileSize 10MB")
    @NotNull(message = "fileSize required")
    private Integer fileSize;
}
