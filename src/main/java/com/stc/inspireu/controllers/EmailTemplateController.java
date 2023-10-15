package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostEmailTemplatesDto;
import com.stc.inspireu.dtos.PostEmailTemplatesTypeDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.services.EmailTemplateService;
import com.stc.inspireu.utils.ConstantUtility;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/${api.version}/management")
public class EmailTemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";
    private final EmailTemplateService emailService;

    @GetMapping("_emailTemplates/emailTemplatesTypes/{emailTemplatesTypeId}/templates")
    public ResponseEntity<?> getTemplatesByType(@RequestParam(defaultValue = "") String name,
                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                @RequestParam(defaultValue = "200") Integer pageSize,
                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                @RequestParam(defaultValue = "createdOn") String sortBy,
                                                @RequestParam(defaultValue = "") String filterBy, HttpServletRequest httpServletRequest,
                                                @PathVariable Long emailTemplatesTypeId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return emailService.getTemplatesByType(currentUserObject, emailTemplatesTypeId, name, paging);
    }

    @GetMapping("_emailTemplates/emailTemplatesTypes/{emailTemplatesTypeId}/templates/{key}")
    public ResponseEntity<?> getTemplatesByTypeAndKey(@RequestParam(defaultValue = "") String name,
                                                      @RequestParam(defaultValue = "0") Integer pageNo,
                                                      @RequestParam(defaultValue = "200") Integer pageSize,
                                                      @RequestParam(defaultValue = "asc") String sortDir,
                                                      @RequestParam(defaultValue = "createdOn") String sortBy,
                                                      @RequestParam(defaultValue = "") String filterBy, HttpServletRequest httpServletRequest,
                                                      @PathVariable Long emailTemplatesTypeId,
                                                      @PathVariable String key) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return emailService.getTemplatesByTypeAndKey(currentUserObject, emailTemplatesTypeId, key, paging);
    }

    @PutMapping("_emailTemplates/emailTemplatesTypes/{emailTemplatesTypeId}/templates/{key}/{templateId}")
    public ResponseEntity<?> publishTemplate(@PathVariable Long emailTemplatesTypeId,
                                             @PathVariable String key,
                                             @PathVariable Long templateId) {
        return emailService.activateTemplate(emailTemplatesTypeId, key, templateId);
    }

    @GetMapping("_emailTemplates/emailTemplatesTypes")
    public ResponseEntity<?> getEmailTypes(@RequestParam(defaultValue = "") String name,
                                           @RequestParam(defaultValue = "0") Integer pageNo,
                                           @RequestParam(defaultValue = "50") Integer pageSize,
                                           @RequestParam(defaultValue = "asc") String sortDir,
                                           @RequestParam(defaultValue = "createdOn") String sortBy,
                                           @RequestParam(defaultValue = "") String filterBy, HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return emailService.getEmailTypes(currentUserObject, name, paging);
    }

    @PostMapping("_emailTemplates/emailTemplatesTypes/publish")
    public ResponseEntity<?> publishEmailTemplateTypes(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestBody PostEmailTemplatesTypeDto postEmailTemplatesTypeDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);
            postEmailTemplatesTypeDto.setStatus(Constant.PUBLISHED.toString());
            return emailService.postEmailTemplateTypes(postEmailTemplatesTypeDto, currentUserObject);
        }

    }

    @DeleteMapping("_emailTemplates/emailTemplatesTypes/{emailTemplateTypesId}")
    public ResponseEntity<?> deleteEmailTemplateTypes(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long emailTemplateTypesId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return emailService.deleteEmailTemplateType(emailTemplateTypesId, currentUserObject);
    }

    @PostMapping("_emailTemplates/publish")
    public ResponseEntity<?> publishEmailTemplate2(HttpServletRequest httpServletRequest,
                                                   @Valid @RequestBody PostEmailTemplatesDto postEmailTemplateDto,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);
            postEmailTemplateDto.setStatus(Constant.PUBLISHED.toString());
            return emailService.postEmailTemplate(postEmailTemplateDto, currentUserObject);
        }

    }

    @GetMapping("_emailTemplates")
    public ResponseEntity<?> getEmailTemplatesById(@RequestParam(defaultValue = "") String name,
                                                   @RequestParam @Valid String emailTemplateContentType,
                                                   @RequestParam @Valid Long emailTemplateTypeId,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @RequestParam(defaultValue = "asc") String sortDir,
                                                   @RequestParam(defaultValue = "createdOn") String sortBy,
                                                   @RequestParam(defaultValue = "") String filterBy,
                                                   HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return emailService.getEmail(currentUserObject, name, emailTemplateContentType, emailTemplateTypeId, paging);
    }

    @GetMapping("_emailTemplates/{emailTemplateId}")
    public ResponseEntity<?> getEmail(HttpServletRequest httpServletRequest, @PathVariable Long emailTemplateId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return emailService.getEmailById(emailTemplateId, currentUserObject);
    }

    @DeleteMapping("_emailTemplates/{emailTemplateId}")
    public ResponseEntity<?> deleteEmailTemplate(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long emailTemplateId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return emailService.deleteEmailTemplate(emailTemplateId, currentUserObject);
    }

    @GetMapping("/_emailTemplates/types")
    public ResponseEntity<Object> getMessageTypes() {
        return ResponseWrapper.response(ConstantUtility.getEmailTemplateContentTypes());
    }

    @PostMapping(value = "_emailTemplates/")
    public ResponseEntity<?> saveTemplateWithAttachments(HttpServletRequest httpServletRequest,
                                                         @RequestParam @NotBlank @Size(min = 1, max = 255) String templateName,
                                                         @RequestParam(required = false) @Nullable List<MultipartFile> attachments,
                                                         @RequestParam @NotBlank @Size(min = 1, max = 255) String subject,
                                                         @RequestParam @NotEmpty String content,
                                                         @RequestParam @NotNull String header,
                                                         @RequestParam @NotNull String footer,
                                                         @RequestParam String language,
                                                         @RequestParam(required = false) @Nullable Long intakeNumber,
                                                         @RequestParam @NotEmpty String templateKey) {
        PostEmailTemplatesDto dto = new PostEmailTemplatesDto();
        dto.setTemplateKey(templateKey);
        dto.setTemplateName(templateName);
        dto.setSubject(subject);
        dto.setContent(content);
        dto.setHeader(header);
        dto.setLanguage(language);
        dto.setStatus("INACTIVE");
        dto.setFooter(footer);
        dto.setIntakeNumber(intakeNumber);
        dto.setAttachments(attachments);
        return emailService.postEmailTemplate(dto, (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT));
    }

    @PutMapping(value = "_emailTemplates/{emailTemplateId}")
    public ResponseEntity<?> updateTemplateWithAttachments(HttpServletRequest httpServletRequest,
                                                           @RequestParam @NotBlank @Size(min = 1, max = 255) String templateName,
                                                           @RequestParam(required = false) @Nullable List<MultipartFile> attachments,
                                                           @RequestParam @NotBlank @Size(min = 1, max = 255) String subject,
                                                           @RequestParam @NotEmpty String content,
                                                           @RequestParam @NotNull String header,
                                                           @RequestParam @NotNull String footer,
                                                           @RequestParam(required = false) @Nullable Long intakeNumber,
                                                           @RequestParam @NotEmpty String templateKey,
                                                           @RequestParam String language,
                                                           @RequestParam(required = false) @Nullable List<Long> deletedFileIds,
                                                           @PathVariable Long emailTemplateId) {
        PostEmailTemplatesDto dto = new PostEmailTemplatesDto();
        dto.setTemplateKey(templateKey);
        dto.setTemplateName(templateName);
        dto.setSubject(subject);
        dto.setLanguage(language);
        dto.setContent(content);
        dto.setHeader(header);
        dto.setFooter(footer);
        dto.setIntakeNumber(intakeNumber);
        dto.setDeletedFileIds(deletedFileIds);
        dto.setAttachments(attachments);
        return emailService.updateEmailTemplate(dto, emailTemplateId,
            (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT));
    }
}
