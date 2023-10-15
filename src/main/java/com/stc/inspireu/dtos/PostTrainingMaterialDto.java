package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostTrainingMaterialDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    private String name;

    private String description;

    @NotNull(message = "trainingfiles required")
    private MultipartFile trainingfile;
}
