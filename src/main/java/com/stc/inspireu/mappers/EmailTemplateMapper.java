package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.EmailAttachmentDTO;
import com.stc.inspireu.dtos.GetEmailTemplateDto2;
import com.stc.inspireu.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {User.class, EmailTemplatesTypes.class, IntakeProgram.class})
public interface EmailTemplateMapper {

    @Mapping(source = "id", target = "templateId")
    @Mapping(source = "name", target = "templateName")
    @Mapping(source = "key", target = "templatekey")

    @Mapping(source = "createdUser.alias", target = "createdUser")

    @Mapping(source = "emailTemplatesTypes.id", target = "emailTemplateTypeId")
    @Mapping(source = "emailTemplatesTypes.name", target = "emailTemplateType")

    @Mapping(source = "intakeProgram.id", target = "intakeNumberId")
    @Mapping(target = "intakeProgram", expression = "java(e.getIntakeProgram() != null " +
        "? e.getIntakeProgram().getProgramName() : \"\")")


    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(target = "attachments", expression = "java(mapAttachments(e.getAttachments(), e.getId()))")
    GetEmailTemplateDto2 toGetEmailTemplateDto2(EmailTemplate e);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    default List<EmailAttachmentDTO> mapAttachments(Set<EmailTemplateAttachment> attachments, Long templateId) {
        if (attachments != null) {
            return attachments.stream()
                .map(attachment -> new EmailAttachmentDTO(attachment.getId(), attachment.getName(),
                    "email_template_attachments/" + templateId + "/" + attachment.getName()))
                .collect(Collectors.toList());
        }
        return null;
    }

}
