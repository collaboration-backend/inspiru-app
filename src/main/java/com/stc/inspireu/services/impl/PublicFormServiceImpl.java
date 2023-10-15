package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.mappers.OpenEventMapper;
import com.stc.inspireu.mappers.OpenEventSlotMapper;
import com.stc.inspireu.models.*;
import com.stc.inspireu.repositories.*;
import com.stc.inspireu.services.CityService;
import com.stc.inspireu.services.CountryService;
import com.stc.inspireu.services.PublicFormService;
import com.stc.inspireu.utils.Encryption;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicFormServiceImpl implements PublicFormService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IntakeProgramSubmissionRepository intakeProgramSubmissionRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final FileAdapter fileAdapter;

    @Value("${api.version}")
    private String apiVersion;
    private final NotificationServiceImpl notificationService;
    private final CountryService countryService;
    private final CityService cityService;
    private final OpenEventRepository openEventRepository;
    private final OpenEventSlotRepository openEventSlotRepository;
    private final Utility utility;
    private final JudgeCalenderRepository judgeCalenderRepository;
    private final OpenEventSlotMapper openEventSlotMapper;
    private final OpenEventMapper openEventMapper;

    @Transactional
    @Override
    public Object intakeProgramRegistration(CurrentUserObject currentUserObject,
                                            PostIntakeProgramRegistrationDto postIntakeProgramRegistrationDto) {
        Map<String, Object> metaData = currentUserObject.getMetaData();
        if (metaData.get("intakeProgramPhase").equals(Constant.REGISTRATION.toString())) {
            Long ipId = Long.parseLong(metaData.get("intakeProgramId").toString());
            IntakeProgram intakeProgram = intakeProgramRepository.findById(ipId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
            List<IntakeProgramSubmission> ipss = intakeProgramSubmissionRepository
                .findByIntakeProgram_IdAndPhaseAndEmail(ipId, Constant.REGISTRATION.toString(),
                    postIntakeProgramRegistrationDto.getEmail());
            if (ipss.size() == 0) {
                IntakeProgramSubmission ips = new IntakeProgramSubmission();
                ips.setIntakeProgram(intakeProgram);
                ips.setPhase(Constant.REGISTRATION.toString());
                ips.setEmail(postIntakeProgramRegistrationDto.getEmail());
                ips.setLanguage(postIntakeProgramRegistrationDto.getLanguage());
                ips.setProfileInfoJson(postIntakeProgramRegistrationDto.getProfileInfoJson());
                ips.setJsonRegistrationForm(postIntakeProgramRegistrationDto.getJsonForm());
                if (intakeProgram.getAssessmentEvaluationForm() != null) {
                    ips.setJsonAssessmentEvaluationForm(intakeProgram.getAssessmentEvaluationForm().getJsonForm());
                }
                if (intakeProgram.getBootcampEvaluationForm() != null) {
                    ips.setJsonBootcampEvaluationForm(intakeProgram.getBootcampEvaluationForm().getJsonForm());
                }
                if (intakeProgram.getProgressReport() != null) {
                    ips.setJsonProgressReport(intakeProgram.getProgressReport().getJsonReportDetail());
                }
                if (Objects.nonNull(intakeProgram.getScreeningEvaluationForm()))
                    ips.setJsonScreeningEvaluationForm(intakeProgram.getScreeningEvaluationForm().getJsonForm());
                intakeProgramSubmissionRepository.save(ips);
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", currentUserObject.getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(currentUserObject.getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("Registration Form submitted");
                mailMetadata.setTemplateFile("Registration Form submitted");
                notificationService.registrationFormSubmittedNotification(mailMetadata, postIntakeProgramRegistrationDto.getLanguage());
                return ips;
            }
            return "already registered";
        }
        return null;
    }

    @Autowired
    private RegistrationFormRepository registrationFormRepository;

    @Transactional
    @Override
    public ResponseEntity<?> getFormInfo(CurrentUserObject currentUserObject, Long formId,
                                         String formTemplateType) {
        switch (formTemplateType) {
            case "registrationForm":
                RegistrationForm registrationForm = registrationFormRepository.findById(formId)
                    .orElseThrow(() -> new CustomRunTimeException("Form not found", HttpStatus.NOT_FOUND));
                if (registrationForm.getDueDate().compareTo(new Date()) < 0)
                    throw new CustomRunTimeException("Form expired", HttpStatus.BAD_REQUEST);
                IntakeProgram intakeProgram = registrationForm.getIntakeProgram();
                if (Objects.isNull(intakeProgram))
                    throw new CustomRunTimeException("Intake not found", HttpStatus.BAD_REQUEST);
                if (!intakeProgram.getStatus().equals(Constant.PUBLISHED.name()) ||
                    !registrationForm.getStatus().equals(Constant.PUBLISHED.name()))
                    throw new CustomRunTimeException("Form is not published", HttpStatus.BAD_REQUEST);
                Map<String, Object> data = new HashedMap<String, Object>();
                data.put("intakeProgramId", intakeProgram.getId());
                data.put("intakeProgramName", intakeProgram.getProgramName());
                data.put("periodStart", intakeProgram.getPeriodStart().toInstant().toEpochMilli());
                data.put("periodEnd", intakeProgram.getPeriodEnd().toInstant().toEpochMilli());
                data.put("status", intakeProgram.getStatus());
                data.put("jsonForm", registrationForm.getJsonForm());
                data.put("language", registrationForm.getLanguage());
                data.put("description", registrationForm.getDescription());
                data.put("banner", Objects.nonNull(registrationForm.getBanner()) ? (apiVersion + "/general/cdn/" + Encryption.encrypt(registrationForm.getBanner())) : null);
                return ResponseWrapper.response(data);
            case "evaluationForm":
            case "bootCampForm":
                break;
        }
        return ResponseWrapper.response400("invalid formTemplateType", "formTemplateType");
    }

    @Transactional
    @Override
    public ResponseEntity<Object> _intakeProgramRegistration(
        CreateIntakeProgramRegistrationDto createIntakeProgramRegistrationDto) {
        RegistrationForm form = registrationFormRepository.findById(createIntakeProgramRegistrationDto.getFormId())
            .orElseThrow(() -> new CustomRunTimeException("Form not found", HttpStatus.NOT_FOUND));
        if (form.getDueDate().compareTo(new Date()) < 0)
            throw new CustomRunTimeException("Form is expired", HttpStatus.BAD_REQUEST);
        IntakeProgram intakeProgram = form.getIntakeProgram();
        if (Objects.isNull(intakeProgram))
            throw new CustomRunTimeException("No intake found", HttpStatus.BAD_REQUEST);
        if (!intakeProgram.getStatus().equals(Constant.PUBLISHED.name()) ||
            !form.getStatus().equals(Constant.PUBLISHED.name()))
            throw new CustomRunTimeException("Form is not published");
        MultipartFile[] files = createIntakeProgramRegistrationDto.getFiles();
        if (files != null) {
            String emailDomain = "";
            for (MultipartFile multipartFile : files) {
                fileAdapter.saveRegFormFile(multipartFile, intakeProgram.getId(), emailDomain);
            }
        }
        IntakeProgramSubmission ips = new IntakeProgramSubmission();
        ips.setBeneficiaryId(createIntakeProgramRegistrationDto.getBeneficiaryId());
        ips.setIntakeProgram(intakeProgram);
        ips.setPhase(Constant.REGISTRATION.toString());
        ips.setEmail(createIntakeProgramRegistrationDto.getEmail());
        ips.setLanguage(createIntakeProgramRegistrationDto.getLanguage());
        ips.setProfileInfoJson(createIntakeProgramRegistrationDto.getProfileInfoJson());
        ips.setStartupName(createIntakeProgramRegistrationDto.getStartupName());
        ips.setJsonRegistrationForm(createIntakeProgramRegistrationDto.getJsonForm());
        if (intakeProgram.getAssessmentEvaluationForm() != null) {
            ips.setJsonAssessmentEvaluationForm(intakeProgram.getAssessmentEvaluationForm().getJsonForm());
        } else
            ips.setJsonAssessmentEvaluationForm("");
        if (intakeProgram.getBootcampEvaluationForm() != null) {
            ips.setJsonBootcampEvaluationForm(intakeProgram.getBootcampEvaluationForm().getJsonForm());
        } else
            ips.setJsonBootcampEvaluationForm("");
        if (intakeProgram.getProgressReport() != null) {
            ips.setJsonProgressReport(intakeProgram.getProgressReport().getJsonReportDetail());
        } else ips.setJsonProgressReport("");
        if (Objects.nonNull(intakeProgram.getScreeningEvaluationForm()))
            ips.setJsonScreeningEvaluationForm(intakeProgram.getScreeningEvaluationForm().getJsonForm());
        else
            ips.setJsonScreeningEvaluationForm("");
        intakeProgramSubmissionRepository.save(ips);
        MailMetadata mailMetadata = new MailMetadata();
        Map<String, Object> props = new HashMap<>();
        props.put("toMail", createIntakeProgramRegistrationDto.getEmail());
        mailMetadata.setFrom("");
        mailMetadata.setTo(createIntakeProgramRegistrationDto.getEmail());
        mailMetadata.setProps(props);
        mailMetadata.setSubject("Registration Form submitted");
        mailMetadata.setTemplateFile("Registration Form submitted");
        notificationService.registrationFormSubmittedNotification(mailMetadata, createIntakeProgramRegistrationDto.getLanguage());
        return ResponseWrapper.response(null, "registration success");
    }

    @Transactional
    @Override
    public ResponseEntity<Object> openEventBooking(CurrentUserObject currentUserObject,
                                                   OpenEventBookingDto openEventBookingDto) {
        OpenEvent oe = openEventRepository.findById(openEventBookingDto.getOpenEventId()).orElseThrow(() -> new CustomRunTimeException("Event not found"));
        if (openEventSlotRepository
            .existsByOpenEvent_IdAndEmail(openEventBookingDto.getOpenEventId(), openEventBookingDto.getEmail())) {
            return ResponseWrapper.response400("Startup Already booked a slot", "OpenEventSlotId");
        }
        if (openEventSlotRepository.existsByOpenEvent_IdAndDay(
            openEventBookingDto.getOpenEventId(), new Date(openEventBookingDto.getStartDatetime()))) {
            return ResponseWrapper.response400("Slot Already Taken By Other", "OpenEventSlotId");
        }
        List<IntakeProgramSubmission> ips = intakeProgramSubmissionRepository
            .findByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(oe.getIntakeProgram().getId(),
                openEventBookingDto.getEmail(), oe.getEventPhase());

        if (ips.isEmpty()) {
            return ResponseWrapper.response400("Invalid Email or Intake", "email|intake");
        }
        List<JudgeCalendar> jcs = judgeCalenderRepository
            .findByIntakeProgram_IdAndEmailAndPhaseContainingIgnoreCase(oe.getIntakeProgram().getId(),
                openEventBookingDto.getEmail(), oe.getEventPhase());
        if (jcs.isEmpty()) {
            return ResponseWrapper.response400("Invalid Email or Intake", "email|intake");
        }
        OpenEventSlot oes = new OpenEventSlot();
        oes.setEmail(openEventBookingDto.getEmail());
        oes.setDay(new Date(openEventBookingDto.getStartDatetime()));
        oes.setEndTimeHour(openEventBookingDto.getEndTimeHour());
        oes.setEndTimeMinute(openEventBookingDto.getEndTimeMinute());
        oes.setStartTimeHour(openEventBookingDto.getStartTimeHour());
        oes.setStartTimeMinute(openEventBookingDto.getStartTimeMinute());
        oes.setOpenEvent(oe);
        openEventSlotRepository.save(oes);
        IntakeProgramSubmission ips1 = ips.get(0);
        if (oe.getEventPhase().equals(Constant.ASSESSMENT.toString())) {
            ips1.setInterviewStart(new Date(openEventBookingDto.getStartDatetime()));
            ips1.setInterviewEnd(new Date(openEventBookingDto.getEndDatetime()));
            intakeProgramSubmissionRepository.save(ips1);
        } else {
            ips1.setInterviewStartBootcamp(new Date(openEventBookingDto.getStartDatetime()));
            ips1.setInterviewEndBootcamp(new Date(openEventBookingDto.getEndDatetime()));
            intakeProgramSubmissionRepository.save(ips1);
        }
        JudgeCalendar jc1 = jcs.get(0);
        jc1.setSessionStart(new Date(openEventBookingDto.getStartDatetime()));
        jc1.setSessionEnd(new Date(openEventBookingDto.getEndDatetime()));
        judgeCalenderRepository.save(jc1);
        return ResponseWrapper.response(null, "done");
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getOpenEventBooking(CurrentUserObject currentUserObject) {
        Map<String, Object> metaData = currentUserObject.getMetaData();
        try {
            Long openEventId = Long.parseLong(metaData.get("openEventId").toString());
            Optional<OpenEvent> oe = openEventRepository.findById(openEventId);
            return oe.map(openEvent -> ResponseWrapper.response(openEventMapper.toGetOpenEventDto(openEvent)))
                .orElseGet(() -> ResponseWrapper.response401("Invalid formToken", "formToken"));
        } catch (Exception e) {
            return ResponseWrapper.response401("Invalid formToken", "formToken");
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getOpenEventDay(CurrentUserObject currentUserObject, Long openEventId,
                                                  Long dateInMilli, String timezone) {
        Map<String, Object> metaData = currentUserObject.getMetaData();
        try {
            Long id = Long.parseLong(metaData.get("openEventId").toString());
            Optional<OpenEvent> e = openEventRepository.findById(id);
            List<OpenEventSlot> ls = new ArrayList<>();
            if (e.isPresent()) {
                Map<String, Date> startAndEnd = utility.atStartAndEndOfDayByMilli(dateInMilli, timezone);
                Iterable<OpenEventSlot> list = openEventSlotRepository.getBookings(startAndEnd.get("start"),
                    startAndEnd.get("end"), id);
                return ResponseWrapper.response(openEventSlotMapper.toGetOpenEventSlotDtoList(list));
            }
            return ResponseWrapper.response(ls);
        } catch (Exception e) {
            return ResponseWrapper.response401("Invalid formToken", "formToken");
        }
    }

    @Override
    public ResponseEntity<?> locationLookup(Long cityId, String iso2CountryCode, String stateCode) throws Exception {
        Map<String, String> ll = new HashedMap<>();
        String r = "";
        CountryDTO c = countryService.findByCountryCode(iso2CountryCode);
        List<CityJsonDTO> ct = cityService.findByIdAndCountryCodeAndStateCode(cityId,
            iso2CountryCode, stateCode);
        if (ct.iterator().hasNext()) {
            r = r + ct.iterator().next().getName() + ",";
        }
        if (c != null) {
            r = r + c.getCountryName();
        }
        ll.put("id", iso2CountryCode + "/" + stateCode + "/" + cityId);
        ll.put("label", r);
        return ResponseWrapper.response(ll);
    }

}
