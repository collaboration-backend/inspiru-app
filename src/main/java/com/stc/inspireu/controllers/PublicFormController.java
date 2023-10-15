package com.stc.inspireu.controllers;

import com.google.gson.Gson;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO_form_fieldGroup;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options;
import com.stc.inspireu.enums.Activity;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.jpa.projections.ProjectRevenueModelByKeyValueLabel;
import com.stc.inspireu.jpa.projections.ProjectSegmentByKeyValueLabel;
import com.stc.inspireu.jpa.projections.ProjectStatusByKeyValueLabel;
import com.stc.inspireu.models.ActivityLog;
import com.stc.inspireu.repositories.ActivityLogRepository;
import com.stc.inspireu.repositories.RevenueModelRepository;
import com.stc.inspireu.repositories.SegmentRepository;
import com.stc.inspireu.repositories.StatusRepository;
import com.stc.inspireu.services.*;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/${api.version}/publicForms")
@RequiredArgsConstructor
@Validated
public class PublicFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DueDiligenceService dueDiligenceService;

    private final CountryService countryService;

    private final ActivityLogRepository activityLogRepository;

    private final CityService cityService;

    private final RevenueModelRepository revenueModelRepository;

    private final StatusRepository statusRepository;

    private final SegmentRepository segmentRepository;

    private final PublicFormService publicFormService;

    private final RegistrationFormService registrationFormService;

    private final Utility utility;

    @PutMapping("dueDiligences/clone")
    public ResponseEntity<Object> _cloneDueDiligence(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> data = dueDiligenceService.cloneDueDiligence(currentUserObject);

        return ResponseWrapper.response(data);
    }

    @GetMapping("startups/dueDiligences/{dueDiligenceId}/fields")
    public ResponseEntity<?> _getDueDiligenceFields(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> metaData = currentUserObject.getMetaData();

        try {
            Long _dueDiligenceId = Long.parseLong(metaData.get("dueDiligenceId").toString());
            if (!_dueDiligenceId.equals(dueDiligenceId)) {
                return ResponseWrapper.response401("invalid formToken", "formToken");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseWrapper.response401("invalid formToken", "formToken");
        }

        return dueDiligenceService.getPublicDueDiligenceFields(currentUserObject, dueDiligenceId);

    }

    @GetMapping("startups/dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents")
    public ResponseEntity<Object> _getDueDiligenceDocuments(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long dueDiligenceId, @PathVariable String fieldId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> metaData = currentUserObject.getMetaData();

        try {
            Long _dueDiligenceId = Long.parseLong(metaData.get("dueDiligenceId").toString());
            if (!_dueDiligenceId.equals(dueDiligenceId)) {
                return ResponseWrapper.response401("invalid formToken", "formToken");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseWrapper.response401("invalid formToken", "formToken");
        }

        List<?> ls = dueDiligenceService.getPublicDueDiligenceDocuments(currentUserObject, dueDiligenceId, fieldId);

        return ResponseWrapper.response(ls);
    }

    @PostMapping(value = "startups/dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> _createDueDiligence(HttpServletRequest httpServletRequest,
                                                      @RequestParam("multipartFiles") MultipartFile[] multipartFiles, @PathVariable Long dueDiligenceId,
                                                      @PathVariable String fieldId) {

        Map<String, Object> fileCheck = utility.checkFile(multipartFiles);

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> metaData = currentUserObject.getMetaData();

        try {
            Long _dueDiligenceId = Long.parseLong(metaData.get("dueDiligenceId").toString());
            if (!_dueDiligenceId.equals(dueDiligenceId)) {
                return ResponseWrapper.response401("invalid formToken", "formToken");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseWrapper.response401("invalid formToken", "formToken");
        }

        if (multipartFiles.length == 0) {
            return ResponseWrapper.response("required atleast one file", "multipartFiles", HttpStatus.BAD_REQUEST);
        } else if (multipartFiles.length > 5) {
            return ResponseWrapper.response("at a time 5 file maximum", "multipartFiles", HttpStatus.BAD_REQUEST);
        } else {
            Object data = dueDiligenceService.createPublicDueDiligence(currentUserObject, multipartFiles,
                dueDiligenceId, fieldId);
            if (data != null) {
                return ResponseWrapper.response(null, "files uploaded");
            } else {
                return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }

    @DeleteMapping("startups/dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents/{documentId}")
    public ResponseEntity<Object> deleteDueDiligence(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long dueDiligenceId, @PathVariable String fieldId, @PathVariable Long documentId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> metaData = currentUserObject.getMetaData();

        try {
            Long _dueDiligenceId = Long.parseLong(metaData.get("dueDiligenceId").toString());
            if (!_dueDiligenceId.equals(dueDiligenceId)) {
                return ResponseWrapper.response401("invalid formToken", "formToken");
            }
        } catch (Exception e) {
            LOGGER.equals(e.getMessage());
            return ResponseWrapper.response401("invalid formToken", "formToken");
        }

        Integer data = dueDiligenceService.deletePublicDueDiligence(currentUserObject, dueDiligenceId, fieldId,
            documentId);

        if (data != null) {
            return ResponseWrapper.response(null, "document removed");
        } else {
            return ResponseWrapper.response("invaid documentId", "documentId", HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping(value = "startups/dueDiligences/{dueDiligenceId}/upload/confirm")
    public ResponseEntity<Object> confirmDueDiligenceUpload(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> metaData = currentUserObject.getMetaData();
        try {
            Long _dueDiligenceId = Long.parseLong(metaData.get("dueDiligenceId").toString());
            if (!_dueDiligenceId.equals(dueDiligenceId)) {
                return ResponseWrapper.response401("invalid formToken", "formToken");
            }
        } catch (Exception e) {
            LOGGER.equals(e.getMessage());
            return ResponseWrapper.response401("invalid formToken", "formToken");
        }

        Object data = dueDiligenceService.confirmPublicDueDiligenceUpload(currentUserObject, dueDiligenceId);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "DueDiligence uploaded");
        } else {

            return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("countries")
    public ResponseEntity<Object> countryList() throws Exception {
        return ResponseWrapper.response(countryService.findAll());
    }

    @GetMapping(value = "location")
    public ResponseEntity<Object> findLocation(@RequestParam String code) throws Exception {
        CityJsonDTO city = cityService.findById(Long.parseLong(code.split("/")[2]));
//        State state = stateRepository.findByStateCodeAndCountryCode(city.getStateCode(), city.getCountryCode())
//            .orElseThrow(() -> new CustomRunTimeException("State not found", HttpStatus.NOT_FOUND));
        CountryDTO country = countryService.findByCountryCode(city.getCountryCode());
        return ResponseWrapper.response(city.getName() + ", " + country.getCountryName());
    }

    @GetMapping("countries/{iso2CountryCode}")
    public ResponseEntity<Object> getCountry(HttpServletRequest httpServletRequest,
                                             @PathVariable String iso2CountryCode) throws Exception {
        if (iso2CountryCode.equals("other"))
            return ResponseWrapper.response(new CountryDTO("other", "other", "Other", "أخرى", "Other"));
        return ResponseWrapper.response(countryService.findByCountryCode(iso2CountryCode));
    }

    @GetMapping("countries/{iso2CountryCode}/states")
    public ResponseEntity<Object> stateList(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode)
        throws Exception {

//        Iterable<ProjectStateByKeyValueLable> ls = stateRepository.findAllByCountryCode(iso2CountryCode);

        return ResponseWrapper.response(new ArrayList<>());

    }

    @GetMapping("countries/{iso2CountryCode}/states/{stateCode}")
    public ResponseEntity<Object> getState(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode,
                                           @PathVariable String stateCode) throws IOException, org.apache.tomcat.util.json.ParseException {

//        Iterable<ProjectStateByKeyValueLable> ls = stateRepository.findByCountryCodeAndStateCode(iso2CountryCode,
//            stateCode);

        return ResponseWrapper.response(new ArrayList<>());

    }

    @GetMapping("countries/{iso2CountryCode}/states/{stateCode}/cities")
    public ResponseEntity<Object> cityList(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode,
                                           @PathVariable String stateCode) throws Exception {
        return ResponseWrapper.response(cityService.findByCountryCodeAndStateCode(iso2CountryCode, stateCode));

    }

    @GetMapping("countries/{iso2CountryCode}/cities")
    public ResponseEntity<Object> cityListByCountry(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode) throws Exception {
        List<CityJsonDTO> cities = cityService.findByCountryCodeOrderByCityName(iso2CountryCode);
        cities.add(new CityJsonDTO(0L, "Other", "أخرى", "", ""));
        return ResponseWrapper.response(cities);

    }


    @GetMapping("cities/{cityId}")
    public ResponseEntity<Object> findCityById(@PathVariable Long cityId) throws Exception {
        return ResponseWrapper.response(cityService.findById(cityId));
    }

    @GetMapping("countries/{iso2CountryCode}/states/{stateCode}/cities/{cityId}")
    public ResponseEntity<Object> getCity(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode,
                                          @PathVariable String stateCode, @PathVariable Long cityId)
        throws Exception {
        return ResponseWrapper.response(cityService.findByIdAndCountryCodeAndStateCode(cityId,
            iso2CountryCode, stateCode));

    }

    @GetMapping("countries/{iso2CountryCode}/states/{stateCode}/cities/{cityId}/lookup")
    public ResponseEntity<?> locationLookup(HttpServletRequest httpServletRequest, @PathVariable String iso2CountryCode,
                                            @PathVariable String stateCode, @PathVariable Long cityId)
        throws Exception {
        return publicFormService.locationLookup(cityId, iso2CountryCode, stateCode);
    }

    @GetMapping("dropdown/revenueModels")
    @ResponseBody
    public ResponseEntity<Object> dropdownRevenueModels(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/revenueModels");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Iterable<ProjectRevenueModelByKeyValueLabel> ls = revenueModelRepository.getAll();

        return ResponseWrapper.response(ls);

    }

    @GetMapping("dropdown/segments")
    @ResponseBody
    public ResponseEntity<Object> dropdownSegments(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/segments");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Iterable<ProjectSegmentByKeyValueLabel> ls = segmentRepository.getAll();

        return ResponseWrapper.response(ls);
    }

    @GetMapping("dropdown/status")
    @ResponseBody
    public ResponseEntity<Object> dropdownStatus(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/status");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Iterable<ProjectStatusByKeyValueLabel> ls = statusRepository.getAll();

        return ResponseWrapper.response(ls);
    }


    @GetMapping("formTemplates/{formTemplateType}/{formId}")
    @ResponseBody
    public ResponseEntity<?> getFormInfo(HttpServletRequest httpServletRequest,
                                         @PathVariable String formTemplateType,
                                         @PathVariable Long formId,
                                         HttpServletRequest request) {
        LOGGER.info("dropdown/status");

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        ResponseEntity<?> response = publicFormService.getFormInfo(currentUserObject, formId, formTemplateType);
        activityLogRepository.save(new ActivityLog(LocalDateTime.now(), Activity.REGISTRATION_FORM_LOADED, Utility.getClientIpAddress(request)));
        return response;

    }

    @PostMapping(value = "intakePrograms/_registrations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> _intakeProgramRegistration(@Validated CreateIntakeProgramRegistrationDto createIntakeProgramRegistrationDto,
                                                             BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        Map<String, Object> fileCheck = utility.checkFile(createIntakeProgramRegistrationDto.getFiles());
        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }
        validateRegistrationForm(createIntakeProgramRegistrationDto);
        ResponseEntity<Object> response = publicFormService._intakeProgramRegistration(
            createIntakeProgramRegistrationDto);
        activityLogRepository.save(new ActivityLog(LocalDateTime.now(), Activity.REGISTRATION_FORM_SUBMITTED, Utility.getClientIpAddress(request)));
        return response;
    }


    private void validateRegistrationForm(CreateIntakeProgramRegistrationDto createIntakeProgramRegistrationDto) {
        RegistrationFormValidationDTO validationDTO = new Gson().fromJson(createIntakeProgramRegistrationDto.getJsonForm(),
            RegistrationFormValidationDTO.class);
        RegistrationFormDto registrationFormDto = registrationFormService.findFormById(createIntakeProgramRegistrationDto.getFormId());
        RegistrationFormValidationDTO originalForm = new Gson().fromJson(registrationFormDto.getFormJson(), RegistrationFormValidationDTO.class);
        if (!validationDTO.equals(originalForm))
            throw new CustomRunTimeException("Form is tampered");

        validationDTO.getForm().forEach(form -> {
            form.getFieldGroup().forEach(this::validateField);
        });
    }

    private void validateField(RegistrationFormValidationDTO_form_fieldGroup field) {

        if (Objects.isNull(field.getTemplateOptions().get_title()))
            return;
        String question = field.getTemplateOptions().get_title();

        if (field.getTemplateOptions().getRequired().equals(Boolean.TRUE)) {
            if (field.getType().equals("custom-file")) {
                if (field.getFieldGroup().stream().noneMatch(f -> f.getType().equals("custom-file-holding")
                    && Objects.nonNull(f.getDefaultValue()) && !f.getDefaultValue().toString().isEmpty())) {
                    throw new CustomRunTimeException("'" + question + "' is required", HttpStatus.BAD_REQUEST);
                }
            } else {
                if ((Objects.isNull(field.getDefaultValue())) || field.getDefaultValue().toString().isEmpty())
                    throw new CustomRunTimeException("'" + question + "' is required", HttpStatus.BAD_REQUEST);
            }
        }

        if (field.getType().equals("custom-number")
            && Objects.nonNull(field.getDefaultValue())
            && !field.getDefaultValue().toString().isEmpty()
            && !Pattern.compile("[-]?\\d+").matcher(String.valueOf(field.getDefaultValue())).matches()) {
            throw new CustomRunTimeException("Invalid input for question: " + question);
        } else if (field.getType().equals("custom-dropdown")) {
            if (field.getTemplateOptions().getMultiple().equals(Boolean.TRUE)) {
                if (!(field.getDefaultValue() instanceof Collection))
                    throw new CustomRunTimeException("Invalid value received for question: " + question);
                List<Object> valuesInObject = (List<Object>) field.getDefaultValue();
                List<String> values = valuesInObject.stream().map(String::valueOf).collect(Collectors.toList());
                values.forEach(value -> {
                    if (!value.isEmpty() && field.getTemplateOptions().getOptions().stream()
                        .noneMatch(opt -> opt.getValue().equals(value) || opt.getLabel().equals(value)))
                        throw new CustomRunTimeException(value + " is not a valid option for question: " + question);
                });
            } else if (Objects.nonNull(field.getDefaultValue())) {
                if (field.getDefaultValue() instanceof Collection)
                    throw new CustomRunTimeException("Invalid value received for question: " + question);
                String value = String.valueOf(field.getDefaultValue());
                if (!value.isEmpty() && field.getTemplateOptions().getOptions().stream().noneMatch(opt ->
                    opt.getValue().equals(value) || opt.getLabel().equals(value)))
                    throw new CustomRunTimeException(value + " is not a valid option for question: " + question);
            }
        } else if (field.getType().equals("custom-email")
            && Objects.nonNull(field.getDefaultValue())
            && !field.getDefaultValue().toString().isEmpty()
            && !Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$").matcher(String.valueOf(field.getDefaultValue())).matches()) {
            throw new CustomRunTimeException("Expecting a valid email for question: " + question);
        } /*else if (field.getType().equals("custom-mobile")
            && Objects.nonNull(field.getDefaultValue())
            && !field.getDefaultValue().toString().isEmpty()) {
            String[] mobileSplit = field.getDefaultValue().toString().split("-");
            if (mobileSplit.length < 2)
                throw new CustomRunTimeException("Invalid value received for question: " + question);
            String validationMessage = Validations.validate(mobileSplit[0], mobileSplit[1]);
            if (!validationMessage.isEmpty())
                throw new CustomRunTimeException(validationMessage);
        } else if (field.getType().equals("custom-date")
            && Objects.nonNull(field.getDefaultValue())
            && !field.getDefaultValue().toString().isEmpty()) {
            if (!Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(String.valueOf(field.getDefaultValue())).matches())
                throw new CustomRunTimeException("Expecting a valid date for question: " + question);
            String[] dateSplit = field.getDefaultValue().toString().split("-");
            LocalDate dateReceived = LocalDate.of(Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[2]));
            LocalDate currentDate = LocalDate.now();
            if (!dateReceived.isBefore(currentDate))
                throw new CustomRunTimeException("Date can't be a future date for question: " + question);
        }*/
        List<RegistrationFormValidationDTO_form_fieldGroup> subFields = new ArrayList<>();
        if (Objects.nonNull(field.getTemplateOptions().getOptions()) && Objects.nonNull(field.getDefaultValue())) {
            if (field.getDefaultValue() instanceof Collection) {
                List<String> selectedValues = (List<String>) field.getDefaultValue();
                List<RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options> selectedOptions = field.getTemplateOptions()
                    .getOptions().stream().filter(opt -> selectedValues.contains(opt.getValue())).collect(Collectors.toList());
                if (!selectedOptions.isEmpty()) {
                    selectedOptions.forEach(opt -> {
                        if (Objects.nonNull(opt.getSubForm())) {
                            List<RegistrationFormValidationDTO_form_fieldGroup> subFieldsTemp = field.getSubForms().get(opt.getSubForm());
                            if (Objects.nonNull(subFieldsTemp))
                                subFields.addAll(subFieldsTemp);
                        }
                    });
                }
            } else {
                Optional<RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options> option = field.getTemplateOptions()
                    .getOptions().stream().filter(opt -> opt.getValue().equals(field.getDefaultValue().toString())).findFirst();
                option.ifPresent(opt -> {
                    if (Objects.nonNull(opt.getSubForm())) {
                        List<RegistrationFormValidationDTO_form_fieldGroup> subFieldsTemp = field.getSubForms().get(opt.getSubForm());
                        if (Objects.nonNull(subFieldsTemp))
                            subFields.addAll(subFieldsTemp);
                    }
                });
            }
        }
        if (!subFields.isEmpty())
            subFields.forEach(this::validateField);
    }

    @GetMapping(value = "openEvents/calendar")
    public ResponseEntity<Object> getOpenEventBooking(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return publicFormService.getOpenEventBooking(currentUserObject);
    }

    @PostMapping(value = "openEvents/calendar")
    public ResponseEntity<Object> createOpenEventBooking(HttpServletRequest httpServletRequest,
                                                         @Valid @RequestBody OpenEventBookingDto openEventBookingDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return publicFormService.openEventBooking(currentUserObject, openEventBookingDto);
    }

    @GetMapping(value = "openEvents/{openEventId}/calendar/{dateInMilli}")
    public ResponseEntity<Object> getOpenEventDay(HttpServletRequest httpServletRequest, @PathVariable Long
        dateInMilli,
                                                  @PathVariable Long openEventId, @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return publicFormService.getOpenEventDay(currentUserObject, openEventId, dateInMilli, timezone);
    }

}
