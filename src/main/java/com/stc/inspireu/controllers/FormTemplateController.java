package com.stc.inspireu.controllers;

import com.google.gson.Gson;
import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.authorization.PermittedRoles;
import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostBootCampFormDto;
import com.stc.inspireu.dtos.PostEvaluationFormTemplate;
import com.stc.inspireu.dtos.PostProfileCardDto;
import com.stc.inspireu.dtos.RegistrationFormDto;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.services.FormTemplateService;
import com.stc.inspireu.utils.ConstantUtility;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}/management")
public class FormTemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final FormTemplateService formTemplateService;

    @GetMapping("formTemplates/types")
    public ResponseEntity<Object> getFormTemplateTypes() {
        LOGGER.info("getFormTemplateTypes");

        return ResponseWrapper.response(ConstantUtility.getFormTemplateTypeList());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("formTemplates/evaluationForms/publish")
    public ResponseEntity<?> postEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                         @Valid @RequestBody PostEvaluationFormTemplate postEvaluationFormTemplate,
                                                         BindingResult bindingResult) {
        LOGGER.info("postFormTemplates");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        return formTemplateService.createEvaluationFormTemplate(currentUserObject, postEvaluationFormTemplate,
            Constant.PUBLISHED.toString());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/evaluationForms/draft")
    public ResponseEntity<?> draftEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                          @Valid @RequestBody PostEvaluationFormTemplate postEvaluationFormTemplate,
                                                          BindingResult bindingResult) {
        LOGGER.info("postFormTemplates");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        return formTemplateService.createEvaluationFormTemplate(currentUserObject, postEvaluationFormTemplate,
            Constant.DRAFT.toString());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/screeningEvaluationForms/draft")
    public ResponseEntity<?> draftScreeningEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                                   @Valid @RequestBody PostEvaluationFormTemplate postEvaluationFormTemplate,
                                                                   BindingResult bindingResult) {
        LOGGER.info("postFormTemplates");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        return formTemplateService.createScreeningEvaluationFormTemplate(currentUserObject, postEvaluationFormTemplate,
            Constant.DRAFT.toString());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PutMapping("formTemplates/screeningEvaluationForms/{formId}")
    public ResponseEntity<?> updateScreeningEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                                    @Valid @RequestBody PostEvaluationFormTemplate postEvaluationFormTemplate,
                                                                    BindingResult bindingResult,
                                                                    @PathVariable Long formId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        return formTemplateService.updateScreeningEvaluationFormTemplate(currentUserObject, postEvaluationFormTemplate,
            formId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @DeleteMapping("formTemplates/screeningEvaluationForms/{formId}")
    public ResponseEntity<?> deleteScreeningForm(HttpServletRequest httpServletRequest, @PathVariable Long formId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.deleteScreeningForm(currentUserObject, formId);
    }

    @GetMapping("formTemplates/evaluationForms")
    public ResponseEntity<?> getEvaluationTemplate(HttpServletRequest httpServletRequest,
                                                   @RequestParam(defaultValue = "") String name,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @RequestParam(defaultValue = "asc") String sortDir) {
        LOGGER.info("getEvaluationTemplate");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("createdOn")));

        return formTemplateService.getEvaluationTemplates(currentUserObject, name, paging);

    }

    @GetMapping("formTemplates/screeningEvaluationForms")
    public ResponseEntity<?> getScreeningEvaluationTemplate(HttpServletRequest httpServletRequest,
                                                            @RequestParam(defaultValue = "") String name,
                                                            @RequestParam(defaultValue = "0") Integer pageNo,
                                                            @RequestParam(defaultValue = "50") Integer pageSize,
                                                            @RequestParam(defaultValue = "asc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("createdOn")));

        return formTemplateService.getScreeningEvaluationTemplates(currentUserObject, name, paging);


    }

    @GetMapping("formTemplates/evaluationForms/{evaluationFormId}")
    public ResponseEntity<?> getEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long evaluationFormId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getEvaluationFormTemplates(currentUserObject, evaluationFormId);
    }

    @GetMapping("formTemplates/screeningEvaluationForms/{evaluationFormId}")
    public ResponseEntity<?> getScreeningEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                                 @PathVariable Long evaluationFormId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getScreeningEvaluationFormTemplates(currentUserObject, evaluationFormId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PutMapping("formTemplates/evaluationForms/{evaluationFormId}")
    public ResponseEntity<?> updateEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                           @PathVariable Long evaluationFormId,
                                                           @Valid @RequestBody PostEvaluationFormTemplate postEvaluationFormTemplate,
                                                           BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return formTemplateService.updateEvaluationFormTemplate(currentUserObject, postEvaluationFormTemplate,
                evaluationFormId);
        }

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @DeleteMapping("formTemplates/evaluationForms/{evaluationFormId}")
    public ResponseEntity<Object> deleteEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                                @PathVariable Long evaluationFormId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.deleteEvaluationFormTemplates(currentUserObject, evaluationFormId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("formTemplates/evaluationForms/{evaluationFormId}/status/{isPublish}")
    public ResponseEntity<?> publishEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long evaluationFormId,
                                                            @PathVariable Boolean isPublish) {
        LOGGER.info("evaluationFormTemplates update");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        String status = isPublish ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString();
        return formTemplateService.updateEvaluationFormTemplateStatus(currentUserObject, status, evaluationFormId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("formTemplates/screeningEvaluationForms/{evaluationFormId}/status/{isPublish}")
    public ResponseEntity<?> publishScreeningEvaluationFormTemplates(HttpServletRequest httpServletRequest,
                                                                     @PathVariable Long evaluationFormId,
                                                                     @PathVariable Boolean isPublish) {
        LOGGER.info("evaluationFormTemplates update");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        String status = isPublish ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString();
        return formTemplateService.updateScreeningEvaluationFormTemplateStatus(currentUserObject, status, evaluationFormId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("formTemplates/registrationForms/publish")
    public ResponseEntity<?> postRegistrationForm(HttpServletRequest httpServletRequest,
                                                  @Valid @RequestBody RegistrationFormDto registrationFormDto,
                                                  BindingResult bindingResult) {
        LOGGER.info("postFormTemplates");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        return formTemplateService.createRegistrationForm(currentUserObject, registrationFormDto,
            Constant.PUBLISHED.toString());
    }

    private void validateRegistrationForm(String jsonForm) {
        RegistrationFormValidationDTO validationDTO = new Gson().fromJson(jsonForm, RegistrationFormValidationDTO.class);
        validationDTO.getForm().forEach(form -> {
            form.getFieldGroup().forEach(field -> {
                if (Objects.isNull(field.getTemplateOptions().get_title()))
                    throw new CustomRunTimeException("Question is missing", HttpStatus.BAD_REQUEST);
                String question = (field.getTemplateOptions().get_title() instanceof String) ?
                    field.getTemplateOptions().get_title().toString() : "Child Question";
/*

                    1) text-field: max 256 characters
                    2) hint-text: max 500 characters
                    3) hint-text is required if hint is true
                    4) number-input: numbers only
                    5) drop-down/checkbox/radio button:
                        5.A) valid option
                        5.B) single/multiple selection
                    6) Mobile validation
                    7) Email validation
*/

                if (Objects.nonNull(field.getTemplateOptions().getHint()) && field.getTemplateOptions().getHint().equals(Boolean.TRUE)
                    && (Objects.isNull(field.getTemplateOptions().getHint_text()) || field.getTemplateOptions().getHint_text().isEmpty()))
                    throw new CustomRunTimeException("Hint text is missing for question: " + field.getTemplateOptions().get_title(), HttpStatus.BAD_REQUEST);
                if (Objects.nonNull(field.getFields()))
                    field.getFields().forEach(childQuestion -> {
                        if (Objects.isNull(childQuestion.getTemplateOptions().get_title()))
                            throw new CustomRunTimeException("Title is missing for a subquestion of '" + question + "'", HttpStatus.BAD_REQUEST);
                    });
            });
        });
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/registrationForms/draft")
    public ResponseEntity<?> draftRegistrationForm(HttpServletRequest httpServletRequest,
                                                   @RequestParam(required = false) @Nullable MultipartFile banner,
                                                   @RequestParam String registrationFormName,
                                                   @RequestParam(required = false) Long intakePgmId,
                                                   @RequestParam(required = false) @Nullable Long dueDate,
                                                   @RequestParam String formJson,
                                                   @RequestParam(required = false) @Nullable String language,
                                                   @RequestParam(required = false) String description,
                                                   @RequestParam(required = false) @Nullable Long copyForm) {
        validateRegistrationForm(formJson);
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRegistrationFormName(registrationFormName);
        dto.setBanner(banner);
        dto.setFormJson(formJson);
        dto.setLanguage(language);
        dto.setDescription(description);
        dto.setDueDate(dueDate);
        dto.setIntakePgmId(intakePgmId);
        if (Objects.nonNull(copyForm) && copyForm.equals(0L))
            copyForm = null;
        dto.setCopiedFromId(copyForm);
        return formTemplateService.createRegistrationForm(currentUserObject, dto,
            Constant.DRAFT.toString());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("formTemplates/registrationForms/{registrationFormId}/status/{isPublish}")
    public ResponseEntity<?> publishRegistrationForm(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long registrationFormId,
                                                     @PathVariable Boolean isPublish) {
        LOGGER.info("evaluationFormTemplates update");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        String status = isPublish ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString();
        return formTemplateService.updateRegistrationFormStatus(currentUserObject, status, registrationFormId);
    }

    @PutMapping(value = "formTemplates/registrationForms/{registrationFormId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateRegistrationForm(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long registrationFormId,
                                                    @RequestParam(required = false) @Nullable MultipartFile banner,
                                                    @RequestParam String registrationFormName,
                                                    @RequestParam(required = false) Long intakePgmId,
                                                    @RequestParam(required = false) @Nullable Long dueDate,
                                                    @RequestParam String formJson,
                                                    @RequestParam(required = false) @Nullable String language,
                                                    @RequestParam(required = false) String description,
                                                    @RequestParam(required = false, defaultValue = "false") Boolean bannerDeleted,
                                                    @RequestParam(required = false) @Nullable Long copyForm) {
        validateRegistrationForm(formJson);
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setBannerDeleted(bannerDeleted);
        dto.setRegistrationFormName(registrationFormName);
        dto.setBanner(banner);
        dto.setDescription(description);
        dto.setFormJson(formJson);
        dto.setLanguage(language);
        dto.setDueDate(dueDate);
        dto.setIntakePgmId(intakePgmId);
        dto.setCopiedFromId(copyForm);
        return formTemplateService.updateRegistrationForm(currentUserObject, dto,
            registrationFormId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @DeleteMapping("formTemplates/registrationForms/{registrationFormId}")
    public ResponseEntity<?> deleteRegistrationForm(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long registrationFormId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.deleteRegistrationForm(currentUserObject, registrationFormId);
    }

    @GetMapping("formTemplates/registrationForms/{registrationFormId}")
    public ResponseEntity<?> getRegistrationForm(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long registrationFormId) {
        LOGGER.info("registrationForm");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getRegistrationForm(currentUserObject, registrationFormId);
    }

    @GetMapping("formTemplates/registrationForms")
    public ResponseEntity<?> getRegistrationForms(HttpServletRequest httpServletRequest,
                                                  @RequestParam(defaultValue = "") String name,
                                                  @RequestParam(defaultValue = "0") Integer pageNo,
                                                  @RequestParam(defaultValue = "50") Integer pageSize,
                                                  @RequestParam(defaultValue = "asc") String sortDir) {
        LOGGER.info("getRegistrationForms");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("createdOn")));

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getRegistrationForms(currentUserObject, name, paging);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/bootCampForms/publish")
    public ResponseEntity<?> postBootCampFormTemplates(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestBody PostBootCampFormDto postBootCampFormDto,
                                                       BindingResult bindingResult) {
        LOGGER.info("postBootCampForms");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return formTemplateService.createBootCampForm(currentUserObject, postBootCampFormDto,
            Constant.PUBLISHED.toString());

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/bootCampForms/draft")
    public ResponseEntity<?> draftBootCampFormTemplates(HttpServletRequest httpServletRequest,
                                                        @Valid @RequestBody PostBootCampFormDto bootCampFormDto,
                                                        BindingResult bindingResult) {
        LOGGER.info("postBootCampForms");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return formTemplateService.createBootCampForm(currentUserObject, bootCampFormDto, Constant.DRAFT.toString());

    }

    @GetMapping("formTemplates/bootCampForms")
    public ResponseEntity<?> getBootCampForms(HttpServletRequest httpServletRequest,
                                              @RequestParam(defaultValue = "") String name,
                                              @RequestParam(defaultValue = "0") Integer pageNo,
                                              @RequestParam(defaultValue = "50") Integer pageSize,
                                              @RequestParam(defaultValue = "asc") String sortDir) {
        LOGGER.info("getBootCampForms");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("createdOn")));

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getBootCampForms(currentUserObject, name, paging);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PutMapping("formTemplates/bootCampForms/{bootCampFormId}/status/{isPublish}")
    public ResponseEntity<?> publishBootCampForms(HttpServletRequest httpServletRequest,
                                                  @PathVariable Long bootCampFormId,
                                                  @PathVariable Boolean isPublish) {
        LOGGER.info("BootCampForms update");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        String status = Boolean.TRUE.equals(isPublish) ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString();

        return formTemplateService.updateBootCampFormStatus(currentUserObject, status, bootCampFormId);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PutMapping("formTemplates/bootCampForms/{bootCampFormId}")
    public ResponseEntity<?> updateBootCampForms(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long bootCampFormId,
                                                 @Valid @RequestBody PostBootCampFormDto bootCampFormDto,
                                                 BindingResult bindingResult) {
        LOGGER.info("update bootCampForms");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return formTemplateService.updateBootCampForm(currentUserObject, bootCampFormDto, bootCampFormId);
        }

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @DeleteMapping("formTemplates/bootCampForms/{bootCampFormId}")
    public ResponseEntity<?> deleteBootCampForm(HttpServletRequest httpServletRequest,
                                                @PathVariable Long bootCampFormId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.deleteBootCampForm(currentUserObject, bootCampFormId);
    }

    @GetMapping("formTemplates/bootCampForms/{bootCampFormId}")
    public ResponseEntity<?> getbootCampForm(HttpServletRequest httpServletRequest, @PathVariable Long bootCampFormId) {
        LOGGER.info("bootCampForm");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getBootCampForm(currentUserObject, bootCampFormId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("formTemplates/profileCards/publish")
    public ResponseEntity<?> postProfileCard(HttpServletRequest httpServletRequest,
                                             @Valid @RequestBody PostProfileCardDto profileCardDto,
                                             BindingResult bindingResult) {
        LOGGER.info("postProfileCard");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return formTemplateService.createProfileCard(currentUserObject, profileCardDto, Constant.PUBLISHED.toString());
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("formTemplates/profileCards/draft")
    public ResponseEntity<?> draftProfileCard(HttpServletRequest httpServletRequest,
                                              @Valid @RequestBody PostProfileCardDto profileCardDto,
                                              BindingResult bindingResult) {
        LOGGER.info("postProfileCard");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return formTemplateService.createProfileCard(currentUserObject, profileCardDto, Constant.DRAFT.toString());
    }

    @GetMapping("formTemplates/profileCards")
    public ResponseEntity<?> getProfileCards(HttpServletRequest httpServletRequest,
                                             @RequestParam(defaultValue = "") String name,
                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "50") Integer pageSize,
                                             @RequestParam(defaultValue = "asc") String sortDir) {
        LOGGER.info("getProfileCards");

        Pageable paging = PageRequest.of(pageNo, pageSize);

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getProfileCards(currentUserObject, name, paging);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("formTemplates/profileCards/{profileCardId}/status/{isPublish}")
    public ResponseEntity<?> publishProfileCard(HttpServletRequest httpServletRequest,
                                                @PathVariable Long profileCardId,
                                                @PathVariable Boolean isPublish) {
        LOGGER.info("profileCard update status");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        String status = isPublish ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString();

        return formTemplateService.updateProfileCardStatus(currentUserObject, status, profileCardId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("formTemplates/profileCards/{profileCardId}")
    public ResponseEntity<?> updateProfileCard(HttpServletRequest httpServletRequest,
                                               @PathVariable Long profileCardId,
                                               @Valid @RequestBody PostProfileCardDto profileCardDto,
                                               BindingResult bindingResult) {
        LOGGER.info("update profileCard");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return formTemplateService.updateProfileCard(currentUserObject, profileCardDto, profileCardId);
        }

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @DeleteMapping("formTemplates/profileCards/{profileCardId}")
    public ResponseEntity<?> deleteProfileCard(HttpServletRequest httpServletRequest,
                                               @PathVariable Long profileCardId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.deleteProfileCard(currentUserObject, profileCardId);

    }

    @GetMapping("formTemplates/profileCards/{profileCardId}")
    public ResponseEntity<?> getProfileCard(HttpServletRequest httpServletRequest, @PathVariable Long profileCardId) {
        LOGGER.info("bootCampForm");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return formTemplateService.getProfileCard(currentUserObject, profileCardId);

    }
}
