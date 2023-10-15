package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CreateIntakeProgramRegistrationDto implements Serializable {


    private static final long serialVersionUID = 1L;

    @NotNull
    private Long formId;

    private Long beneficiaryId;

    @NotEmpty
    private String startupName;

    @NotBlank(message = "jsonForm required")
    private String jsonForm;

    @Email(message = "Given input is not a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    private String language;

    @NotBlank(message = "profileInfoJson required")
    private String profileInfoJson;

    private MultipartFile[] files;

}
