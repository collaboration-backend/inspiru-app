package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostEmailTemplatesDto;
import com.stc.inspireu.dtos.PostEmailTemplatesTypeDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.EmailTemplateMapper;
import com.stc.inspireu.mappers.EmailTemplatesTypesMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.EmailTemplateService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final KeyValueRepository keyValueRepository;
    private final EmailTemplateTypesRepository emailTemplateTypesRepository;
    private final UserRepository userRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final FileAdapter fileAdapter;
    private final EmailTemplatesTypesMapper emailTemplatesTypesMapper;
    private final EmailTemplateMapper emailTemplateMapper;

    @Override
    public ResponseEntity<?> getEmailTypes(CurrentUserObject userObject, String name, Pageable page) {
        userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<EmailTemplatesTypes> list = Objects.equals(name, "") ? emailTemplateTypesRepository.findAll(page)
            : emailTemplateTypesRepository.findByNameContainingIgnoreCase(name, page);
        return ResponseWrapper.response(list.map(emailTemplatesTypesMapper::toGetEmailTemplatesTypesDto));
    }

    @Override
    public ResponseEntity<?> postEmailTemplateTypes(PostEmailTemplatesTypeDto postEmailTemplatesTypeDto,
                                                    CurrentUserObject userObject) {
        User user = userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        EmailTemplatesTypes emailTemplatesTypes = new EmailTemplatesTypes();
        emailTemplatesTypes.setName(postEmailTemplatesTypeDto.getName());
        emailTemplatesTypes.setStatus(postEmailTemplatesTypeDto.getStatus());
        emailTemplatesTypes.setCreatedUser(user);
        return ResponseWrapper.response(emailTemplateTypesRepository.save(emailTemplatesTypes));
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteEmailTemplateType(Long id, CurrentUserObject userObject) {
        userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<EmailTemplatesTypes> result = emailTemplateTypesRepository.findById(id);
        if (result.isPresent()) {
            try {
                emailTemplateTypesRepository.deleteById(id);
                return ResponseWrapper.response("deleted Successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseWrapper.response(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return ResponseWrapper.response(id + " not found", "EmailTemplate Id", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> getEmail(CurrentUserObject userObject, String name, String emailContentType,
                                      Long emailTemplateTypeId, Pageable page) {
        userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<EmailTemplate> list = Objects.equals(name, "")
            ? emailTemplateRepository.findByEmailContentTypeAndEmailTemplatesTypes_Id(emailContentType,
            emailTemplateTypeId, page)
            : emailTemplateRepository
            .findByNameContainingIgnoreCaseAndEmailContentTypeAndEmailTemplatesTypes_Id(name,
                emailContentType, emailTemplateTypeId, page);
        return ResponseWrapper.response(list.map(emailTemplateMapper::toGetEmailTemplateDto2));
    }

    private void validateAttachments(List<MultipartFile> files) {
        if (Objects.nonNull(files) && !files.isEmpty()) {
            KeyValue filesAllowed = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());
            KeyValue fileSize = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());
            if (Objects.nonNull(filesAllowed)) {
                List<String> allowedFileExtensions = Arrays.asList(filesAllowed.getValueName().split(","));
                files.forEach(f -> {
                    String extension = "." + FilenameUtils.getExtension(f.getOriginalFilename());
                    if (allowedFileExtensions.stream().noneMatch(extension::equalsIgnoreCase))
                        throw new CustomRunTimeException(extension + " format not supported", HttpStatus.BAD_REQUEST);
                });
            }
            if (Objects.nonNull(fileSize)) {
                long maxSize = Long.parseLong(fileSize.getValueName());
                files.forEach(f -> {
                    long size = f.getSize() / 1000000;
                    if (size > maxSize) {
                        throw new CustomRunTimeException("Max supported file size is " + maxSize + " MB", HttpStatus.BAD_REQUEST);
                    }
                });
            }
        }
    }

    @Override
    public ResponseEntity<?> postEmailTemplate(PostEmailTemplatesDto postEmailTemplateDto,
                                               CurrentUserObject userObject) {
        User user = userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        validateAttachments(postEmailTemplateDto.getAttachments());
        Optional<IntakeProgram> intakeProgram = intakeProgramRepository.findById(
            postEmailTemplateDto.getIntakeNumber() != null ? postEmailTemplateDto.getIntakeNumber() : 0);
        EmailTemplate templateWithSameName = emailTemplateRepository.findByKeyAndNameIgnoreCase(postEmailTemplateDto.getTemplateKey(), postEmailTemplateDto.getTemplateName());
        if (Objects.nonNull(templateWithSameName))
            return ResponseWrapper.response("Template '" + templateWithSameName.getName() + "' already exists", HttpStatus.BAD_REQUEST);
        EmailTemplate existingActiveTemplate = emailTemplateRepository.findByKeyAndLanguage(postEmailTemplateDto.getTemplateKey(), Objects.nonNull(postEmailTemplateDto.getLanguage()) ? postEmailTemplateDto.getLanguage() : "en")
            .orElseThrow(() -> new CustomRunTimeException("No active template found"));
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setName(postEmailTemplateDto.getTemplateName());
        emailTemplate.setStatus(postEmailTemplateDto.getStatus());
        emailTemplate.setLanguage(Objects.nonNull(postEmailTemplateDto.getLanguage()) ? postEmailTemplateDto.getLanguage() : "en");
        emailTemplate
            .setEmailTemplatesTypes(existingActiveTemplate.getEmailTemplatesTypes());
        emailTemplate.setEmailContentType(existingActiveTemplate.getEmailContentType());
        emailTemplate.setCreatedUser(user);
        emailTemplate.setFooter(postEmailTemplateDto.getFooter());
        emailTemplate.setContent(postEmailTemplateDto.getContent());
        emailTemplate.setHeader(postEmailTemplateDto.getHeader());
        emailTemplate.setSubject(postEmailTemplateDto.getSubject());
        emailTemplate.setKey(postEmailTemplateDto.getTemplateKey());
        emailTemplate.setIntakeProgram(intakeProgram.orElse(null));
        if (Objects.nonNull(postEmailTemplateDto.getAttachments())) {
            for (MultipartFile file : postEmailTemplateDto.getAttachments()) {
                EmailTemplateAttachment attachment = new EmailTemplateAttachment();
                attachment.setTemplate(emailTemplate);
                attachment.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
                emailTemplate.getAttachments().add(attachment);
            }
        }
        EmailTemplate result = emailTemplateRepository.save(emailTemplate);
        if (Objects.nonNull(postEmailTemplateDto.getAttachments())) {
            fileAdapter.uploadFiles(postEmailTemplateDto.getAttachments(), "email_template_attachments/" + result.getId());
        }
        return ResponseWrapper.response("Email Template saved", HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> updateEmailTemplate(PostEmailTemplatesDto postEmailTemplateDto, Long emailTemplateId,
                                                 CurrentUserObject userObject) {
        userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        validateAttachments(postEmailTemplateDto.getAttachments());
        Optional<EmailTemplate> result = emailTemplateRepository.findById(emailTemplateId);
        if (result.isPresent()) {
            EmailTemplate emailTemplate = result.get();
            EmailTemplate templateWithSameName = emailTemplateRepository.findByKeyAndNameIgnoreCase(emailTemplate.getKey(), postEmailTemplateDto.getTemplateName());
            if (Objects.nonNull(templateWithSameName) && !templateWithSameName.equals(emailTemplate))
                return ResponseWrapper.response("Template '" + templateWithSameName.getName() + "' already exists", HttpStatus.BAD_REQUEST);
            emailTemplate.setName(postEmailTemplateDto.getTemplateName());
            if (Objects.nonNull(postEmailTemplateDto.getLanguage()))
                emailTemplate.setLanguage(postEmailTemplateDto.getLanguage());
            emailTemplate.setFooter(postEmailTemplateDto.getFooter());
            emailTemplate.setContent(postEmailTemplateDto.getContent());
            emailTemplate.setSubject(postEmailTemplateDto.getSubject());
            emailTemplate.setHeader(postEmailTemplateDto.getHeader());
            if (Objects.nonNull(postEmailTemplateDto.getAttachments())) {
                for (MultipartFile file : postEmailTemplateDto.getAttachments()) {
                    EmailTemplateAttachment attachment = new EmailTemplateAttachment();
                    attachment.setTemplate(emailTemplate);
                    attachment.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
                    emailTemplate.getAttachments().add(attachment);
                }
            }
            List<String> filesToBeRemoved = new ArrayList<>();
            if (Objects.nonNull(postEmailTemplateDto.getDeletedFileIds())) {
                Set<EmailTemplateAttachment> attachmentsToDelete = new HashSet<>();
                emailTemplate.getAttachments().stream()
                    .filter(file -> postEmailTemplateDto.getDeletedFileIds().contains(file.getId()))
                    .forEach(attachmentsToDelete::add);
                emailTemplate.getAttachments().removeAll(attachmentsToDelete);
                attachmentsToDelete.forEach(a -> filesToBeRemoved
                    .add("email_template_attachments/" + emailTemplate.getId() + "/" + a.getName()));
            }
            EmailTemplate response = emailTemplateRepository.save(emailTemplate);
            if (!filesToBeRemoved.isEmpty())
                fileAdapter.deleteFiles(filesToBeRemoved);
            if (Objects.nonNull(postEmailTemplateDto.getAttachments())) {
                fileAdapter.uploadFiles(postEmailTemplateDto.getAttachments(),
                    "email_template_attachments/" + response.getId());
            }
            return ResponseWrapper.response("Email Template saved", HttpStatus.OK);
        }
        return ResponseWrapper.response(emailTemplateId + " not found", "email Template", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> deleteEmailTemplate(Long id, CurrentUserObject userObject) {
        userRepository.findById(userObject.getUserId())
            .orElseThrow(() -> new CustomRunTimeException("User not found", HttpStatus.NOT_FOUND));
        EmailTemplate result = emailTemplateRepository.findById(id)
            .orElseThrow(() -> new CustomRunTimeException("Template not found", HttpStatus.NOT_FOUND));
        if (result.getStatus().equals("ACTIVE"))
            throw new CustomRunTimeException("Active template can't be deleted", HttpStatus.BAD_REQUEST);
        Set<EmailTemplateAttachment> attachments = result.getAttachments();
        List<String> filesToBeRemoved = null;
        if (Objects.nonNull(attachments)) {
            filesToBeRemoved = attachments.stream().map(f -> "email_template_attachments/" + result.getId() + "/" + f.getName()).collect(Collectors.toList());
        }
        emailTemplateRepository.delete(result);
        if (Objects.nonNull(filesToBeRemoved))
            fileAdapter.deleteFiles(filesToBeRemoved);
        return ResponseWrapper.response("Deleted Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getEmailById(Long id, CurrentUserObject userObject) {
        userRepository.findById(userObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<EmailTemplate> result = emailTemplateRepository.findById(id);
        return result.map(emailTemplate -> ResponseWrapper.response(emailTemplateMapper.toGetEmailTemplateDto2(emailTemplate)))
            .orElseGet(() -> ResponseWrapper.response("No Data", HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<?> getTemplatesByType(CurrentUserObject currentUserObject, Long emailTemplatesTypeId,
                                                String name, Pageable paging) {
        Page<EmailTemplate> ls = emailTemplateRepository.findByEmailTemplatesTypes_IdAndStatus(emailTemplatesTypeId, "ACTIVE", paging);
        Page<com.stc.inspireu.dtos.GetEmailTemplateDto2> list = ls.map(emailTemplateMapper::toGetEmailTemplateDto2);
        return ResponseWrapper.response(list);
    }

    @Override
    public ResponseEntity<?> getTemplatesByTypeAndKey(CurrentUserObject currentUserObject, Long
        emailTemplatesTypeId, String key, Pageable paging) {
        return ResponseWrapper.response(
            emailTemplateRepository.findByEmailTemplatesTypes_IdAndKey(emailTemplatesTypeId, key, paging)
                .map(emailTemplateMapper::toGetEmailTemplateDto2)
        );
    }

    @Transactional
    @Override
    public ResponseEntity<?> activateTemplate(Long emailTemplatesTypeId, String key, Long templateId) {
        Optional<EmailTemplate> emailTemplate2 = emailTemplateRepository.findByIdAndKey(templateId, key);
        if (!emailTemplate2.isPresent())
            return ResponseWrapper.response("Template not found", HttpStatus.BAD_REQUEST);
        EmailTemplate template = emailTemplate2.get();
        if (template.getStatus().equals("ACTIVE"))
            return ResponseWrapper.response("Template is already activated", HttpStatus.BAD_REQUEST);
        emailTemplateRepository.inactivateAllTemplatesByKeyAndTypeAndLanguage(key, emailTemplatesTypeId, template.getLanguage());
        template.setStatus("ACTIVE");
        emailTemplateRepository.save(template);
        return ResponseWrapper.response(null, "Template successfully activated");
    }

    @Override
    public Optional<com.stc.inspireu.dtos.GetEmailTemplateDto2> findActiveEmailTemplateByKeyAndLanguage(String key, String language) {
        EmailTemplate emailTemplate = emailTemplateRepository.findByKeyAndLanguage(key, language).orElse(null);
        if (Objects.nonNull(emailTemplate)) {
            return Optional.of(emailTemplateMapper.toGetEmailTemplateDto2(emailTemplate));
        }
        return Optional.empty();
    }

}
