package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class GetEmailTemplateDto2 implements Serializable {

    private Long templateId;

    private String templateName;

    private String language;

    private String subject;

    private String templatekey;

    private String content;

    private String footer;

    private String header;

    private String intakeProgram;

    private Long intakeNumberId;

    private String emailTemplateType;

    private Long emailTemplateTypeId;

    private String emailContentType;

    private String createdUser;

    private Long CreatedAt;

    private String status;

    private List<EmailAttachmentDTO> attachments = new ArrayList<>();

    private List<String> attachmentPaths;

    public List<String> getAttachmentPaths() {
        if (Objects.nonNull(attachments) && !attachments.isEmpty()) {
            attachmentPaths = attachments.stream().map(a -> "email_template_attachments/" + templateId + "/" + a.getName())
                .collect(Collectors.toList());
        }
        return attachmentPaths;
    }

    public void setAttachmentPaths(List<String> attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

}

