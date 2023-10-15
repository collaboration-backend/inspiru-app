package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.*;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.FormTemplateService;
import com.stc.inspireu.utils.Encryption;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FormTemplateServiceImpl implements FormTemplateService {

    private final IntakeProgramRepository intakeProgramRepository;
    private final RegistrationFormRepository registrationFormRepository;
    private final BootcampFormRepository bootcampFormRepository;
    private final FileAdapter fileAdapter;
    private final AssessmentEvaluationFormRepository assessmentEvaluationFormRepository;
    private final ScreeningEvaluationFormRepository screeningEvaluationFormRepository;
    private final ProfileCardRepository profileCardRepository;
    private final UserRepository userRepository;
    private final AssessmentEvaluationFormRepository evaluationFormRepository;
    private final ScreeningEvaluationFormMapper screeningEvaluationFormMapper;
    private final RegistrationFormMapper registrationFormMapper;
    private final ProfileCardMapper profileCardMapper;
    private final AssessmentEvaluationFormMapper assessmentEvaluationFormMapper;
    private final BootcampEvaluationFormMapper bootcampEvaluationFormMapper;

    @Value("${api.version}")
    private String apiVersion;

    @Transactional
    @Override
    public ResponseEntity<?> updateEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                          PostEvaluationFormTemplate postEvaluationFormTemplate, Long id) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return assessmentEvaluationFormRepository.findById(id).map(form -> {
            if (!form.getStatus().equals(Constant.DRAFT.toString())) {
                return ResponseWrapper.response400("Only DRAFT can be editable", "evaluationFormId");
            }
            form.setFormName(postEvaluationFormTemplate.getEvaluationTemplateName());
            form.setJsonForm(postEvaluationFormTemplate.getFormJson());
            AssessmentEvaluationForm e = assessmentEvaluationFormRepository.save(form);
            Map<String, Object> data = new HashMap<>();
            data.put("formId", e.getId());
            data.put("formName", e.getFormName());
            data.put("jsonForm", e.getJsonForm());
            data.put("intakeProgramId", null);
            if (e.getIntakeProgram() != null) {
                data.put("intakeProgramId", e.getIntakeProgram().getId());
            }
            return ResponseWrapper.response(data, "Successfully Saved");
        }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateEvaluationFormTemplateStatus(CurrentUserObject currentUserObject, String status,
                                                                Long id) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return assessmentEvaluationFormRepository.findById(id)
            .map(form -> {
                form.setStatus(status);
                form.setPublishedUser(user);
                form.setPublishedAt(new Date());
                assessmentEvaluationFormRepository.save(form);
                return ResponseWrapper.response(assessmentEvaluationFormRepository.save(form).getStatus());
            }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Override
    public ResponseEntity<?> updateScreeningEvaluationFormTemplateStatus(CurrentUserObject currentUserObject, String status, Long id) {
        User user = userRepository.findById(currentUserObject.getUserId())
            .orElseThrow(() -> new CustomRunTimeException("You are not authorized", HttpStatus.FORBIDDEN));
        ScreeningEvaluationForm formTemplate = screeningEvaluationFormRepository.findById(id)
            .orElseThrow(() -> new CustomRunTimeException("Form not found", HttpStatus.NOT_FOUND));
        if (formTemplate.getStatus().equals(status))
            throw new CustomRunTimeException("Form already in " + status + " status");
        formTemplate.setStatus(status);
        formTemplate.setPublishedUser(user);
        formTemplate.setPublishedAt(new Date());
        return ResponseWrapper.response(screeningEvaluationFormRepository.save(formTemplate).getStatus(), "Status successfully updated");
    }

    @Transactional
    @Override
    public ResponseEntity<?> createEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                          PostEvaluationFormTemplate postFormTemplateDto, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = Optional.empty();
        if (Objects.nonNull(postFormTemplateDto.getIntakePgmId())) {
            intakeProgram = intakeProgramRepository.findById(postFormTemplateDto.getIntakePgmId());
        }
        AssessmentEvaluationForm a;
        if (intakeProgram.isPresent()) {
            a = assessmentEvaluationFormRepository
                .findByIntakeProgram_IdAndFormNameIgnoreCase(postFormTemplateDto.getIntakePgmId(),
                    postFormTemplateDto.getEvaluationTemplateName());
        } else {
            a = assessmentEvaluationFormRepository
                .findByIntakeProgramIsNullAndFormNameIgnoreCase(
                    postFormTemplateDto.getEvaluationTemplateName());
        }
        if (Objects.nonNull(a)) {
            return ResponseWrapper.response400("Name Already Exist", "formName");
        }
        AssessmentEvaluationForm evaluationForm = new AssessmentEvaluationForm();
        evaluationForm.setFormName(postFormTemplateDto.getEvaluationTemplateName());
        evaluationForm.setEvaluationPhase(postFormTemplateDto.getEvaluationPhase());
        evaluationForm.setJsonForm(postFormTemplateDto.getFormJson());
        evaluationForm.setCreatedUser(user);
        if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
            evaluationForm.setPublishedUser(user);
            evaluationForm.setPublishedAt(new Date());
        }
        evaluationForm.setIntakeProgram(intakeProgram.orElse(null));
        evaluationForm.setStatus(status);
        AssessmentEvaluationForm e = assessmentEvaluationFormRepository.save(evaluationForm);
        return ResponseWrapper.response(assessmentEvaluationFormMapper.toGetAssessmentEvaluationFormDto(e));
    }

    @Transactional
    @Override
    public ResponseEntity<?> createScreeningEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                                   PostEvaluationFormTemplate postFormTemplateDto, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram intakeProgram = postFormTemplateDto.getIntakePgmId() != null ?
            intakeProgramRepository.findById(postFormTemplateDto.getIntakePgmId()).orElse(null) : null;
        if (screeningEvaluationFormRepository
            .existsByIntakeProgram_IdAndFormNameIgnoreCase(postFormTemplateDto.getIntakePgmId(),
                postFormTemplateDto.getEvaluationTemplateName())) {
            return ResponseWrapper.response400("Name Already Exist", "formName");
        }
        ScreeningEvaluationForm evaluationForm = new ScreeningEvaluationForm();
        evaluationForm.setFormName(postFormTemplateDto.getEvaluationTemplateName());
        evaluationForm.setEvaluationPhase(postFormTemplateDto.getEvaluationPhase());
        evaluationForm.setJsonForm(postFormTemplateDto.getFormJson());
        evaluationForm.setCreatedUser(user);
        if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
            evaluationForm.setPublishedUser(user);
            evaluationForm.setPublishedAt(new Date());
        }
        evaluationForm.setIntakeProgram(intakeProgram);
        evaluationForm.setStatus(status);
        if (Objects.nonNull(intakeProgram)) {
            intakeProgram.setScreeningEvaluationForm(evaluationForm);
            evaluationForm = intakeProgramRepository.save(intakeProgram).getScreeningEvaluationForm();
        } else {
            evaluationForm = screeningEvaluationFormRepository.save(evaluationForm);
        }
        return ResponseWrapper.response(screeningEvaluationFormMapper.toScreeningEvaluationFormDTO(evaluationForm));
    }

    @Override
    public ResponseEntity<?> updateScreeningEvaluationFormTemplate(CurrentUserObject currentUserObject,
                                                                   PostEvaluationFormTemplate postFormTemplateDto, Long formId) {
        if (!userRepository.existsById(currentUserObject.getUserId()))
            throw new CustomRunTimeException("You are not authorized", HttpStatus.FORBIDDEN);
        IntakeProgram intakeProgram = postFormTemplateDto.getIntakePgmId() != null ?
            intakeProgramRepository.findById(postFormTemplateDto.getIntakePgmId()).orElse(null) : null;
        ScreeningEvaluationForm evaluationForm = screeningEvaluationFormRepository.findById(formId)
            .orElseThrow(() -> new CustomRunTimeException("Screening form not found", HttpStatus.NOT_FOUND));
        Optional<ScreeningEvaluationForm> formWithSameName = screeningEvaluationFormRepository
            .findByFormNameIgnoreCase(postFormTemplateDto.getEvaluationTemplateName());
        if (formWithSameName.isPresent() && !formWithSameName.get().equals(evaluationForm))
            throw new CustomRunTimeException("Form with same name already exists");
        if (evaluationForm.getStatus().equals(Constant.PUBLISHED.name()))
            throw new CustomRunTimeException("You cannot update a published form");
        evaluationForm.setFormName(postFormTemplateDto.getEvaluationTemplateName());
        evaluationForm.setEvaluationPhase(postFormTemplateDto.getEvaluationPhase());
        evaluationForm.setJsonForm(postFormTemplateDto.getFormJson());
        evaluationForm.setIntakeProgram(intakeProgram);
        if (Objects.nonNull(intakeProgram)) {
            intakeProgram.setScreeningEvaluationForm(evaluationForm);
            evaluationForm = intakeProgramRepository.save(intakeProgram).getScreeningEvaluationForm();
        } else {
            evaluationForm = screeningEvaluationFormRepository.save(evaluationForm);
        }
        return ResponseWrapper.response(screeningEvaluationFormMapper.toScreeningEvaluationFormDTO(evaluationForm));
    }

    @Transactional
    @Override
    public ResponseEntity<?> createRegistrationForm(CurrentUserObject currentUserObject,
                                                    RegistrationFormDto registrationFormDto, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = Optional.empty();
        if (registrationFormDto.getIntakePgmId() != null) {
            intakeProgram = intakeProgramRepository.findById(registrationFormDto.getIntakePgmId());
        }
        registrationFormRepository.findByFormName(registrationFormDto.getRegistrationFormName()).ifPresent((registrationForm) -> {
            throw new CustomRunTimeException(registrationFormDto.getRegistrationFormName() + " already exists");
        });
        RegistrationForm registrationForm = new RegistrationForm();
        setValuesToForm(registrationForm, registrationFormDto);
        registrationForm.setCreatedUser(user);
        intakeProgram.ifPresent(registrationForm::setIntakeProgram);
        registrationForm.setStatus(status);
        if (Objects.nonNull(registrationForm.getIntakeProgram()))
            registrationForm.setDueDate(registrationForm.getIntakeProgram().getPeriodEnd());
        registrationForm.setLanguage(registrationFormDto.getLanguage());
        if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
            registrationForm.setPublishedUser(user);
            registrationForm.setPublishedAt(new Date());
        }
        RegistrationForm e = registrationFormRepository.save(registrationForm);
        return ResponseWrapper.response(registrationFormMapper.toGetRegistrationFormDto(e));
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateRegistrationForm(CurrentUserObject currentUserObject,
                                                    RegistrationFormDto registrationFormDto, Long id) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        RegistrationForm form = registrationFormRepository.findById(id).orElseThrow(() -> new CustomRunTimeException("Form not found", HttpStatus.NOT_FOUND));
        registrationFormRepository.findByFormName(registrationFormDto.getRegistrationFormName()).ifPresent((registrationForm) -> {
            if (!form.equals(registrationForm)) {
                throw new CustomRunTimeException(registrationFormDto.getRegistrationFormName() + " already exists");
            }
        });
        if (!form.getStatus().equals(Constant.DRAFT.toString())) {
            return ResponseWrapper.response400("Only DRAFT can be editable", "registrationFormId");
        }
        setValuesToForm(form, registrationFormDto);
        RegistrationForm e = registrationFormRepository.save(form);
        Map<String, Object> data = new HashMap<>();
        data.put("formId", e.getId());
        data.put("formName", e.getFormName());
        data.put("jsonForm", e.getJsonForm());
        data.put("intakeProgramId", null);
        if (e.getIntakeProgram() != null) {
            data.put("intakeProgramId", e.getIntakeProgram().getId());
        }
        return ResponseWrapper.response(data, "Successfully Saved");
    }

    private void setValuesToForm(RegistrationForm form, RegistrationFormDto registrationFormDto) {
        if (Objects.nonNull(registrationFormDto.getBanner()) && !registrationFormDto.getBanner().isEmpty()) {
            if (Objects.nonNull(form.getBanner()) && !form.getBanner().isEmpty())
                fileAdapter.deleteFiles(Collections.singletonList(form.getBanner()));
            List<String> supportedImageFormats = Arrays.asList("png", "jpg", "jpeg", "svg");
            final String extension = FilenameUtils.getExtension(registrationFormDto.getBanner().getOriginalFilename());
            if (supportedImageFormats.stream().noneMatch(s -> s.equalsIgnoreCase(extension)))
                throw new CustomRunTimeException("Invalid image file", HttpStatus.BAD_REQUEST);
            form.setBanner(fileAdapter.uploadFile(registrationFormDto.getBanner(), "banner_images", UUID.randomUUID().toString()));
        } else if (Objects.nonNull(registrationFormDto.getBannerDeleted()) && registrationFormDto.getBannerDeleted().equals(Boolean.TRUE)) {
            if (Objects.nonNull(form.getBanner()) && !form.getBanner().isEmpty())
                fileAdapter.deleteFiles(Collections.singletonList(form.getBanner()));
        } else if (Objects.nonNull(registrationFormDto.getCopiedFromId())) {
            RegistrationForm copiedFrom = registrationFormRepository.findById(registrationFormDto.getCopiedFromId())
                .orElseThrow(() -> new CustomRunTimeException("Invalid copied from ID"));
            if (Objects.nonNull(copiedFrom.getBanner()) && !copiedFrom.getBanner().isEmpty()) {
                String randomId = UUID.randomUUID().toString();
                String fileName = randomId + "." + FilenameUtils.getExtension(copiedFrom.getBanner());
                File file = fileAdapter.getFile(copiedFrom.getBanner(), fileName);
                if (Objects.nonNull(file) && file.exists())
                    try {
                        MultipartFile multipartFile = new org.springframework.mock.web.MockMultipartFile(fileName, Files.newInputStream(file.toPath()));
                        form.setBanner(fileAdapter.uploadFile(multipartFile, "banner_images", randomId, FilenameUtils.getExtension(copiedFrom.getBanner())));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
        }
        if (Objects.nonNull(registrationFormDto.getIntakePgmId()))
            form.setIntakeProgram(intakeProgramRepository.findById(registrationFormDto.getIntakePgmId())
                .orElseThrow(() -> new CustomRunTimeException("Intake not found", HttpStatus.NOT_FOUND)));
        form.setFormName(registrationFormDto.getRegistrationFormName());
        form.setJsonForm(registrationFormDto.getFormJson());
        form.setLanguage(registrationFormDto.getLanguage());
        form.setDescription(registrationFormDto.getDescription());
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateRegistrationFormStatus(CurrentUserObject currentUserObject, String status, Long id) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<RegistrationForm> form = registrationFormRepository.findById(id);
        if (form.isPresent()) {
            RegistrationForm registrationForm = form.get();
            registrationForm.setStatus(status);
            if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
                registrationForm.setPublishedUser(user);
                registrationForm.setPublishedAt(new Date());
            }
            registrationFormRepository.save(registrationForm);
            return ResponseWrapper.response("status updated", HttpStatus.OK);
        }
        return ResponseWrapper.response(id + " not found", "registrationFormId", HttpStatus.NOT_FOUND);

    }

    @Transactional
    @Override
    public ResponseEntity<?> createBootCampForm(CurrentUserObject currentUserObject,
                                                PostBootCampFormDto bootCampFormDto, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<IntakeProgram> intakeProgram = Optional.empty();

        if (bootCampFormDto.getIntakeProgramId() != null) {
            intakeProgram = intakeProgramRepository.findById(bootCampFormDto.getIntakeProgramId());
        }
        BootcampEvaluationForm a;
        if (intakeProgram.isPresent()) {
            a = bootcampFormRepository.findByIntakeProgram_IdAndFormNameIgnoreCase(
                bootCampFormDto.getIntakeProgramId(), bootCampFormDto.getFormName());
        } else {
            a = bootcampFormRepository
                .findByIntakeProgramIsNullAndFormNameIgnoreCase(bootCampFormDto.getFormName());
        }
        if (Objects.nonNull(a)) {
            return ResponseWrapper.response400("Name Already Exist", "formName");
        }

        BootcampEvaluationForm bootcampEvaluationForm = new BootcampEvaluationForm();

        bootcampEvaluationForm.setFormName(bootCampFormDto.getFormName());
        bootcampEvaluationForm.setJsonForm(bootCampFormDto.getJsonForm());
        bootcampEvaluationForm.setCreatedUser(user);
        bootcampEvaluationForm.setStatus(status);
        bootcampEvaluationForm.setIntakeProgram(intakeProgram.orElse(null));
        if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
            bootcampEvaluationForm.setPublishedUser(user);
            bootcampEvaluationForm.setPublishedAt(new Date());
        }
        BootcampEvaluationForm e = bootcampFormRepository.save(bootcampEvaluationForm);
        Map<String, Object> data = new HashMap<>();
        data.put("formId", e.getId());
        data.put("formName", e.getFormName());
        data.put("jsonForm", e.getJsonForm());
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateBootCampForm(CurrentUserObject currentUserObject,
                                                PostBootCampFormDto bootCampFormDto, Long id) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return bootcampFormRepository.findById(id)
            .map(form -> {
                if (!form.getStatus().equals(Constant.DRAFT.toString())) {
                    return ResponseWrapper.response400("Only DRAFT can be editable", "bootCampFormId");
                }
                form.setFormName(bootCampFormDto.getFormName());
                form.setJsonForm(bootCampFormDto.getJsonForm());
                BootcampEvaluationForm e = bootcampFormRepository.save(form);
                Map<String, Object> data = new HashMap<>();
                data.put("formId", e.getId());
                data.put("formName", e.getFormName());
                data.put("jsonForm", e.getJsonForm());
                data.put("intakeProgramId", null);
                if (e.getIntakeProgram() != null) {
                    data.put("intakeProgramId", e.getIntakeProgram().getId());
                }
                return ResponseWrapper.response(data, "Successfully Saved");
            }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateBootCampFormStatus(CurrentUserObject currentUserObject, String status, Long id) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return bootcampFormRepository.findById(id)
            .map(form -> {
                form.setStatus(status);
                if (status.equalsIgnoreCase(Constant.PUBLISHED.toString())) {
                    form.setPublishedUser(user);
                    form.setPublishedAt(new Date());
                }
                bootcampFormRepository.save(form);
                return ResponseWrapper.response("status updated", HttpStatus.OK);
            }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getBootCampForms(CurrentUserObject currentUserObject, String name, Pageable paging) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<BootcampEvaluationForm> data = name.isEmpty() ? bootcampFormRepository.findAll(paging)
            : bootcampFormRepository.findByFormNameContainingIgnoreCase(name, paging);
        return ResponseWrapper.response(data.map(bootcampEvaluationFormMapper::toGetBootCampFormsDto));
    }

    @Transactional
    @Override
    public ResponseEntity<?> createProfileCard(CurrentUserObject currentUserObject,
                                               PostProfileCardDto postProfileCardDto, String status) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (profileCardRepository
            .existsByIntakeProgramIsNullAndNameIgnoreCase(postProfileCardDto.getProfileCardName())) {
            return ResponseWrapper.response400("Name Already Exist", "name");
        }
        ProfileCard profileCard = Objects.nonNull(postProfileCardDto.getRefProfileCardId()) ?
            profileCardRepository.findById(postProfileCardDto.getRefProfileCardId())
                .orElse(new ProfileCard()) : new ProfileCard();
        profileCard.setCreatedUser(user);
        profileCard.setName(postProfileCardDto.getProfileCardName());
        profileCard.setStatus(status);
        profileCard.setStartup(null);
        profileCard.setJsonForm(postProfileCardDto.getJsonForm());
        ProfileCard e = profileCardRepository.save(profileCard);
        Map<String, Object> data = new HashMap<>();
        data.put("formId", e.getId());
        data.put("formName", e.getName());
        data.put("jsonForm", e.getJsonForm());
        return ResponseWrapper.response(data);
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateProfileCard(CurrentUserObject currentUserObject,
                                               PostProfileCardDto postProfileCardDto, Long id) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return profileCardRepository.findById(id)
            .map(profileCard -> {
                if (!profileCard.getStatus().equals(Constant.DRAFT.toString())) {
                    return ResponseWrapper.response400("Only DRAFT can be editable", "profileCardId");
                }
                profileCard.setName(postProfileCardDto.getProfileCardName());
                profileCard.setJsonForm(postProfileCardDto.getJsonForm());
                profileCard = profileCardRepository.save(profileCard);
                Map<String, Object> data = new HashMap<>();
                data.put("formId", profileCard.getId());
                data.put("formName", profileCard.getName());
                data.put("jsonForm", profileCard.getJsonForm());
                data.put("intakeProgramId", null);
                if (profileCard.getIntakeProgram() != null) {
                    data.put("intakeProgramId", profileCard.getIntakeProgram().getId());
                }
                return ResponseWrapper.response(data, "Successfully Saved");
            }).orElseThrow(() -> new CustomRunTimeException("Profile card not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateProfileCardStatus(CurrentUserObject currentUserObject, String status, Long id) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (Objects.nonNull(id)) {
            return profileCardRepository.findById(id)
                .map(profileCard -> {
                    profileCard.setStatus(status);
                    ProfileCard result = profileCardRepository.save(profileCard);
                    return ResponseWrapper.response(result.getStatus(), HttpStatus.OK);
                }).orElseThrow(() -> new CustomRunTimeException("Profile card not found"));
        } else {
            return ResponseWrapper.response("Profile Card id is null", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> getProfileCards(CurrentUserObject currentUserObject, String name, Pageable paging) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<ProfileCard> data = name.isEmpty() ? profileCardRepository.findAll(paging)
            : profileCardRepository.findByNameContainingIgnoreCase(name, paging);
        return ResponseWrapper.response(data.map(profileCardMapper::toGetProfileCardDto));

    }

    @Transactional
    @Override
    public ResponseEntity<?> getRegistrationForm(CurrentUserObject currentUserObject, Long registrationFormId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        RegistrationForm registrationForm = registrationFormRepository.findById(registrationFormId)
            .orElseThrow(() -> new CustomRunTimeException("Registration form not found"));
        GetRegistrationFormDto dto = registrationFormMapper.toGetRegistrationFormDto(registrationForm);
        if (Objects.nonNull(registrationForm.getBanner())) {
            dto.setBanner(apiVersion + "/general/cdn/" + Encryption.encrypt(registrationForm.getBanner()));
        }
        return ResponseWrapper.response(dto);
    }

    @Transactional
    @Override
    public ResponseEntity<?> getRegistrationForms(CurrentUserObject currentUserObject, String name, Pageable paging) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<RegistrationForm> data = name.isEmpty() ? registrationFormRepository.findAll(paging)
            : registrationFormRepository.findByFormNameContainingIgnoreCase(name, paging);
        return ResponseWrapper.response(data.map(registrationFormMapper::toGetRegistrationFormDto));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getBootCampForm(CurrentUserObject currentUserObject, Long bootCampFormId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return bootcampFormRepository.findById(bootCampFormId)
            .map(form -> ResponseWrapper.response(bootcampEvaluationFormMapper.toGetBootCampFormsDto(form)))
            .orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getEvaluationFormTemplates(CurrentUserObject currentUserObject, Long evaluationFormId) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return assessmentEvaluationFormRepository.findById(evaluationFormId)
            .map(form -> ResponseWrapper.response(assessmentEvaluationFormMapper.toGetAssessmentEvaluationFormDto(form)))
            .orElseThrow(() -> new CustomRunTimeException("Form not found"));
    }

    @Override
    public ResponseEntity<?> getScreeningEvaluationFormTemplates(CurrentUserObject currentUserObject, Long evaluationFormId) {
        if (!userRepository.existsById(currentUserObject.getUserId())) {
            return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "userId",
                HttpStatus.NOT_FOUND);
        }
        return screeningEvaluationFormRepository.findById(evaluationFormId)
            .map(screeningEvaluationForm -> ResponseWrapper.response(screeningEvaluationFormMapper
                .toScreeningEvaluationFormDTO(screeningEvaluationForm)))
            .orElseGet(() -> ResponseWrapper.response(evaluationFormId + " not found", "evaluationFormId", HttpStatus.NOT_FOUND));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getEvaluationTemplates(CurrentUserObject currentUserObject, String name, Pageable paging) {
        userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Page<AssessmentEvaluationForm> data = name.isEmpty() ? assessmentEvaluationFormRepository.findAll(paging)
            : assessmentEvaluationFormRepository.findByFormNameContainingIgnoreCase(name, paging);
        return ResponseWrapper.response(data.map(assessmentEvaluationFormMapper::toGetAssessmentEvaluationFormDto));
    }

    @Override
    public ResponseEntity<?> getScreeningEvaluationTemplates(CurrentUserObject currentUserObject, String name, Pageable paging) {
        if (!userRepository.existsById(currentUserObject.getUserId()))
            return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "userId",
                HttpStatus.NOT_FOUND);
        Page<ScreeningEvaluationForm> data = (Objects.isNull(name) || name.isEmpty())
            ? screeningEvaluationFormRepository.findAll(paging)
            : screeningEvaluationFormRepository.findByFormNameContainingIgnoreCase(name, paging);
        return ResponseWrapper.response(data.map(screeningEvaluationFormMapper::toScreeningEvaluationFormDTO));
    }

    @Transactional
    @Override
    public ResponseEntity<?> getProfileCard(CurrentUserObject currentUserObject, Long profileCardId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            return profileCardRepository.findById(profileCardId)
                .map(ResponseWrapper::response)
                .orElseThrow(() -> new CustomRunTimeException("Profile card not found"));
        }
        return ResponseWrapper.response(currentUserObject.getUserId() + " not found", "memberId", HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteProfileCard(CurrentUserObject currentUserObject, Long profileCardId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getWillManagement()) {
            return profileCardRepository.findById(profileCardId)
                .map(profileCard -> {
                    if (profileCard.getStatus().equals(Constant.PUBLISHED.toString())) {
                        return ResponseWrapper.response(profileCardId + " profileCard  published", HttpStatus.BAD_REQUEST);
                    }
                    profileCardRepository.removeProfileCardById(profileCardId);
                    return ResponseWrapper.response(null, "profileCard deleted ");
                }).orElseThrow(() -> new CustomRunTimeException("Profile card not found"));
        } else {
            String message = profileCardId == null ? "bootCampFormId required" : "memberId";
            String error = profileCardId == null ? "bootCampFormId" : "not found";
            return ResponseWrapper.response(message, error, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteBootCampForm(CurrentUserObject currentUserObject, Long bootCampFormId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (bootCampFormId != null && user.getWillManagement()) {
            return bootcampFormRepository.findById(bootCampFormId)
                .map(form -> {
                    if (form.getStatus().equals(Constant.PUBLISHED.toString())) {
                        return ResponseWrapper.response(bootCampFormId + " bootCampForm template published",
                            HttpStatus.BAD_REQUEST);
                    }
                    // do deleted if not published
                    bootcampFormRepository.delete(form);
                    return ResponseWrapper.response(null, "bootCampForm deleted ");
                }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
        } else {
            String message = bootCampFormId == null ? "bootCampFormId required" : "memberId";
            String error = bootCampFormId == null ? "bootCampFormId" : "not found";
            return ResponseWrapper.response(message, error, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteRegistrationForm(CurrentUserObject currentUserObject, Long registrationFormId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (registrationFormId != null && user.getWillManagement()) {
            return registrationFormRepository.findById(registrationFormId)
                .map(form -> {
                    if (form.getStatus().equals(Constant.PUBLISHED.toString())) {
                        return ResponseWrapper.response(registrationFormId + "form template published",
                            HttpStatus.BAD_REQUEST);
                    }
                    // do deleted if not published
                    registrationFormRepository.delete(form);
                    return ResponseWrapper.response(null, "registrationForm deleted ");
                }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
        } else {
            String message = registrationFormId == null ? "registrationFormId required" : "memberId";
            String error = registrationFormId == null ? "registrationFormId" : "not found";
            return ResponseWrapper.response(message, error, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> deleteScreeningForm(CurrentUserObject currentUserObject, Long formId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (formId != null && user.getWillManagement()) {
            ScreeningEvaluationForm screeningEvaluationForm = screeningEvaluationFormRepository.findById(formId)
                .orElseThrow(() -> new CustomRunTimeException("Pre assessment form not found", HttpStatus.NOT_FOUND));
            if (screeningEvaluationForm.getStatus().equals(Constant.PUBLISHED.toString())) {
                return ResponseWrapper.response("You can not delete a published form",
                    HttpStatus.BAD_REQUEST);
            }
            screeningEvaluationFormRepository.delete(screeningEvaluationForm);
            return ResponseWrapper.response(null, "Form successfully deleted ");
        } else if (user.getWillManagement().equals(Boolean.FALSE)) {
            return ResponseWrapper.response("User is not permitted", "Unauthorized Action", HttpStatus.BAD_REQUEST);
        } else {
            String message = "formId required";
            String error = "formId";
            return ResponseWrapper.response(message, error, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> deleteEvaluationFormTemplates(CurrentUserObject currentUserObject,
                                                                Long evaluationFormId) {
        User user = userRepository.findById((currentUserObject.getUserId())).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (evaluationFormId != null && user.getWillManagement()) {
            return evaluationFormRepository.findById(evaluationFormId)
                .map(form -> {
                    if (form.getStatus().equals(Constant.PUBLISHED.toString())) {
                        return ResponseWrapper.response(evaluationFormId + "evaluationForm published",
                            HttpStatus.BAD_REQUEST);
                    }
                    // do delete if not published
                    evaluationFormRepository.delete(form);
                    return ResponseWrapper.response(null, "evaluationForm deleted " + evaluationFormId);
                }).orElseThrow(() -> new CustomRunTimeException("Form not found"));
        } else {

            String message = evaluationFormId == null ? "evaluationFormId required" : "memberId";
            String error = evaluationFormId == null ? "evaluationFormId" : "not found";

            return ResponseWrapper.response(message, error, HttpStatus.BAD_REQUEST);
        }
    }

}
