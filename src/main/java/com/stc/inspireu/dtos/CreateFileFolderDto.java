package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Set;

@Data
public class CreateFileFolderDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "name required")
    @Pattern(regexp = "^[a-zA-Z0-9_ -]*$", message = "must be alphanumeric, hypen, space or underscore")
    private String name;

    @NotNull(message = "isFile required")
    private Boolean isFile;

    private String description;

    private MultipartFile[] files;

    private String parentFolderId;

    private Set<@Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "must be alphanumeric, hypen or underscore") String> tags;

    private Set<Long> intakeProgramIds;

    private Long intakeProgramId;
}
