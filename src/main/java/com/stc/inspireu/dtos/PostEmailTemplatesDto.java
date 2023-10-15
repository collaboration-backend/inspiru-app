package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class PostEmailTemplatesDto implements Serializable {

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 255)
    private String templateName;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 255)
    private String subject;

    @NotNull
    @NotEmpty
    private String content;

    private String language;

    @NotNull
    private String header;

    @NotNull
    private String footer;

    private Long intakeNumber;

    @NotNull
    private String templateKey;

    @Nullable
    private List<Long> deletedFileIds;

    @Nullable
    private List<MultipartFile> attachments;

    private String status;
}
