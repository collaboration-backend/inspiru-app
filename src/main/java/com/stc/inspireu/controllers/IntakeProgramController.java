package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.authorization.PermittedRoles;
import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO;
import com.stc.inspireu.dtos.validation.RegistrationFormValidationDTO_form_fieldGroup;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.mappers.IntakeProgramMapper;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.services.CityService;
import com.stc.inspireu.services.CountryService;
import com.stc.inspireu.services.EvaluationSummaryService;
import com.stc.inspireu.services.IntakeProgramService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/${api.version}/management")
@Validated
@RequiredArgsConstructor
public class IntakeProgramController {

    @Autowired
    private IntakeProgramMapper intakeProgramMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final IntakeProgramService intakeProgramService;

    private final CityService cityService;

    private final EvaluationSummaryService evaluationSummaryService;

    private final CountryService countryService;

    private List<CityJsonDTO> cities;

    private List<CountryDTO> countries;

    @GetMapping("intakePrograms/{intakeProgramId}/onboarding")
    public ResponseEntity<Object> listOnboardingStarups(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long intakeProgramId,
                                                        @RequestParam(defaultValue = "0") Integer pageNo,
                                                        @RequestParam(defaultValue = "50") Integer pageSize,
                                                        @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Page<GetIntakeProgramSubmissionDto> ls = intakeProgramService.listOnboardingStarups(currentUserObject,
            intakeProgramId, paging);

        return ResponseWrapper.response(ls);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("intakePrograms/{intakeProgramId}/onboarding")
    public ResponseEntity<?> postIntakeProgramOnboardingEmails(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long intakeProgramId,
                                                               @Valid @RequestBody PostStartupOnboardingDto postOnboardingDto,
                                                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            return intakeProgramService.createStartupOnboarding(currentUserObject, intakeProgramId, postOnboardingDto);
        }
    }

    @GetMapping("intakePrograms")
    public ResponseEntity<?> getIntakePrograms(@RequestParam(defaultValue = "0") Integer pageNo,
                                               HttpServletRequest httpServletRequest,
                                               @RequestParam(defaultValue = "50") Integer pageSize,
                                               @RequestParam(defaultValue = "asc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdOn"));
        return intakeProgramService.getIntakePrograms(currentUserObject, paging);
    }

    @GetMapping("intakePrograms/{intakeProgramId}")
    public ResponseEntity<Object> getIntakeProgram(HttpServletRequest httpServletRequest, @PathVariable Long intakeProgramId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.getIntakeProgram(currentUserObject, intakeProgramId);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("intakePrograms")
    public ResponseEntity<Object> postIntakeProgram(HttpServletRequest httpServletRequest,
                                                    @Valid @RequestBody PostIntakeProgramDto postIntakeProgramDto,
                                                    BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            IntakeProgram ip1 = intakeProgramService.saveIntakeProgram(currentUserObject, postIntakeProgramDto);

            return ResponseWrapper.response(intakeProgramMapper.toGetIntakeProgramDto(ip1));
        }
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}")
    public ResponseEntity<Object> postIntakeProgramAsDraft(HttpServletRequest httpServletRequest,
                                                           @PathVariable Long intakeProgramId,
                                                           @Valid @RequestBody PostIntakeProgramDto postIntakeProgramDto,
                                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            IntakeProgram ip = intakeProgramService.updateIntakeProgram(intakeProgramId, postIntakeProgramDto);

            if (ip != null) {
                return ResponseWrapper.response(intakeProgramMapper.toGetIntakeProgramDto(ip));
            }

            return ResponseWrapper.response400("invalid intakeProgramId", "intakeProgramId");
        }
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @DeleteMapping("intakePrograms/{intakeProgramId}")
    public ResponseEntity<Object> deleteIntake(HttpServletRequest httpServletRequest,
                                               @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.deleteIntake(currentUserObject, intakeProgramId);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/share")
    public ResponseEntity<Object> shareIntake(HttpServletRequest httpServletRequest,
                                              @PathVariable Long intakeProgramId,
                                              @Valid @RequestBody ShareIntakeDto shareIntakeDto,
                                              BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            Object obj = intakeProgramService.shareIntake(currentUserObject, intakeProgramId, shareIntakeDto);

            if (obj instanceof String) {
                return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
            }

            return ResponseWrapper.response(null, "intakeProgram " + intakeProgramId + "shared");
        }

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/publish")
    public ResponseEntity<Object> publishIntake(HttpServletRequest httpServletRequest,
                                                @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.publishIntake(currentUserObject, intakeProgramId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(null, "intakeProgram " + intakeProgramId + "published");

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/bootCampForms/{bootCampFormId}/link")
    public ResponseEntity<Object> linkBootCampForm(HttpServletRequest httpServletRequest,
                                                   @PathVariable Long bootCampFormId,
                                                   @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.linkForm("bootCampForm", currentUserObject, intakeProgramId, bootCampFormId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj, "bootCampForm linked with intakeProgram");
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/profileCards/{profileCardId}/link")
    public ResponseEntity<Object> linkProfileCard(HttpServletRequest httpServletRequest,
                                                  @PathVariable Long profileCardId,
                                                  @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.linkForm("profileCard", currentUserObject, intakeProgramId, profileCardId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj, "bootCampForm linked with intakeProgram");
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/registrationForms/{registrationFormId}/link")
    public ResponseEntity<Object> linkRegistrationForm(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long registrationFormId,
                                                       @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.linkForm("registrationForm", currentUserObject, intakeProgramId,
            registrationFormId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj, "registrationForm linked with intakeProgram");
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/evaluationForms/{evaluationFormId}/link")
    public ResponseEntity<Object> linkEvaluationForm(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long evaluationFormId,
                                                     @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.linkForm("evaluationForm", currentUserObject, intakeProgramId,
            evaluationFormId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj, "evaluationForm linked with intakeProgram");
    }

    @GetMapping("intakePrograms/{intakeProgramId}/registrations")
    public ResponseEntity<Object> registartionSubmissions(HttpServletRequest httpServletRequest,
                                                          @RequestParam(defaultValue = "0") Integer pageNo,
                                                          @RequestParam(defaultValue = "en") String language,
                                                          @RequestParam(defaultValue = "50") Integer pageSize,
                                                          @RequestParam(defaultValue = "asc") String sortDir,
                                                          @RequestParam(defaultValue = "createdOn") String sortBy,
                                                          @RequestParam(defaultValue = "email") String filterBy,
                                                          @RequestParam(defaultValue = "") String filterKeyword,
                                                          @PathVariable Long intakeProgramId) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        Page<GetIntakeProgramSubmissionDto> list = intakeProgramService.registartionSubmissions(currentUserObject, intakeProgramId,
            Constant.REGISTRATION.toString(), language, paging, filterKeyword, filterBy);

        return ResponseWrapper.response(list);
    }

    @GetMapping("intakePrograms/{intakeProgramId}/registrations/export")
    public ResponseEntity<ByteArrayResource> exportApplicationSubmission(@PathVariable Long intakeProgramId,
                                                                         @RequestParam(required = false) List<Long> applicationIds,
                                                                         @RequestHeader String assetToken,
                                                                         @RequestHeader String apiGatewayUrl,
                                                                         @RequestHeader String apiKey) throws Exception {
        List<RegistrationFormValidationDTO> applications = intakeProgramService.intakeProgramApplications(intakeProgramId, applicationIds);
        if (applications.isEmpty())
            return ResponseEntity.ok(null);
        /*
        Grouping submissions by form metadata
         */
        Map<List<String>, List<RegistrationFormValidationDTO>> filteredSubmissions = new LinkedHashMap<>();
        List<Long> cityIds = new ArrayList<>();
        List<String> countryCode = new ArrayList<>();
        RegistrationFormValidationDTO formWithMaxQuestions = applications.get(0);
        applications.forEach(submission -> {
            List<String> questions = new ArrayList<>();
            submission.getForm().get(0).getFieldGroup().forEach(field -> {
                questions.add(field.getTemplateOptions().get_title());
                if (field.getType().equals("custom-city") && Objects.nonNull(field.getDefaultValue()) && !field.getDefaultValue().toString().isEmpty())
                    cityIds.add(Long.valueOf(field.getDefaultValue().toString()));
                else if (field.getType().equals("custom-country") && Objects.nonNull(field.getDefaultValue()) && !field.getDefaultValue().toString().isEmpty())
                    countryCode.add(field.getDefaultValue().toString());
                if (Objects.nonNull(field.getSubForms())) {
                    field.getSubForms().forEach((name, fields) -> fields.forEach(subField -> {
                        questions.add(subField.getTemplateOptions().get_title());
                        if (subField.getType().equals("custom-city") && Objects.nonNull(subField.getDefaultValue()) && !subField.getDefaultValue().toString().isEmpty())
                            cityIds.add(Long.valueOf(subField.getDefaultValue().toString()));
                        else if (subField.getType().equals("custom-country") && Objects.nonNull(subField.getDefaultValue()) && !subField.getDefaultValue().toString().isEmpty())
                            countryCode.add(subField.getDefaultValue().toString());
                        if (Objects.nonNull(subField.getSubForms())) {
                            subField.getSubForms().forEach((name2, fields2) -> fields2.forEach(subField2 ->
                            {
                                questions.add(subField2.getTemplateOptions().get_title());
                                if (subField2.getType().equals("custom-city") && Objects.nonNull(subField2.getDefaultValue()) && !subField2.getDefaultValue().toString().isEmpty())
                                    cityIds.add(Long.valueOf(subField2.getDefaultValue().toString()));
                                else if (subField2.getType().equals("custom-country") && Objects.nonNull(subField2.getDefaultValue()) && !subField2.getDefaultValue().toString().isEmpty())
                                    countryCode.add(subField2.getDefaultValue().toString());
                            }));
                        }
                    }));
                }
            });
            List<RegistrationFormValidationDTO> mapValues = filteredSubmissions.get(questions);
            if (Objects.isNull(mapValues))
                mapValues = new ArrayList<>();
            mapValues.add(submission);
            filteredSubmissions.put(questions, mapValues);
        });


        /*
            Fetching all cities and countries required to reduce the database transactions
         */
        if (!cityIds.isEmpty())
            cities = cityService.findByIds(cityIds);
        if (!countryCode.isEmpty())
            countries = countryService.findAlByCodes(countryCode);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XSSFWorkbook workbook = new XSSFWorkbook();

        /*
            Header row style
         */
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        XSSFSheet sheet = workbook.createSheet("Form 1");
        sheet.setDefaultColumnWidth(40);
        AtomicReference<Integer> rowCount = new AtomicReference<>(0);
        Row headerRow = sheet.createRow(rowCount.get());
        AtomicReference<Integer> headerCellCount = new AtomicReference<>(1);
        Cell createdDateCell = headerRow.createCell(0);
        createdDateCell.setCellValue("Created on");
        createdDateCell.setCellStyle(cellStyle);
        List<String> qKeys = new ArrayList<>();
        formWithMaxQuestions.getForm().get(0).getFieldGroup().forEach(f -> {
            Cell cell = headerRow.createCell(headerCellCount.get());
            cell.setCellValue(f.getTemplateOptions().get_title());
            qKeys.add(f.getKey());
            cell.setCellStyle(cellStyle);
            headerCellCount.set(headerCellCount.get() + 1);
            if (Objects.nonNull(f.getSubForms())) {
                f.getSubForms().forEach((a, fields) -> fields.forEach(f2 -> {
                    Cell subCell = headerRow.createCell(headerCellCount.get());
                    subCell.setCellValue(f2.getTemplateOptions().get_title());
                    qKeys.add(f2.getKey());
                    subCell.setCellStyle(cellStyle);
                    headerCellCount.set(headerCellCount.get() + 1);
                    if (Objects.nonNull(f2.getSubForms())) {
                        f2.getSubForms().forEach((a2, fields2) -> fields2.forEach(f3 -> {
                            qKeys.add(f3.getKey());
                            Cell subCell2 = headerRow.createCell(headerCellCount.get());
                            subCell2.setCellValue(f3.getTemplateOptions().get_title());
                            subCell2.setCellStyle(cellStyle);
                            headerCellCount.set(headerCellCount.get() + 1);
                        }));
                    }
                }));
            }
        });
        rowCount.set(rowCount.get() + 1);
        applications.forEach(submission -> {
            Row row = sheet.createRow(rowCount.get());
            List<RegistrationFormValidationDTO_form_fieldGroup> fields = submission.getForm().get(0).getFieldGroup();
            AtomicReference<Integer> cellCount = new AtomicReference<>(1);
            Cell createdDateValueCell = row.createCell(0);
            createdDateValueCell.setCellValue(submission.getCreatedOn());
            qKeys.forEach(key -> {
                fields.forEach(field -> {
                    if (field.getKey().equals(key)) {
                        setCellValue(row.createCell(cellCount.get()), field, assetToken, apiGatewayUrl, intakeProgramId, apiKey);
                    } else if (Objects.nonNull(field.getSubForms())) {
                        field.getSubForms().forEach((formName, fields2) -> fields2.forEach(field2 -> {
                            if (field2.getKey().equals(key)) {
                                setCellValue(row.createCell(cellCount.get()), field2, assetToken, apiGatewayUrl, intakeProgramId, apiKey);
                            } else if (Objects.nonNull(field2.getSubForms())) {
                                field2.getSubForms().forEach((name, fields3) -> fields3.forEach(field3 -> {
                                    if (field3.getKey().equals(key))
                                        setCellValue(row.createCell(cellCount.get()), field3, assetToken, apiGatewayUrl, intakeProgramId, apiKey);
                                }));
                            }
                        }));
                    }
                });
                cellCount.set(cellCount.get() + 1);
            });
            rowCount.set(rowCount.get() + 1);
        });
        HttpHeaders header = new HttpHeaders();
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Applications.xlsx");
        workbook.write(stream);
        workbook.close();
        return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()),
            header, HttpStatus.OK);
    }

    private void setCellValue(Cell cell, RegistrationFormValidationDTO_form_fieldGroup field, String assetToken,
                              String apiGatewayUrl, Long intakeProgramId, String apiKey) {
        Object value = field.getDefaultValue();
        if (value instanceof Collection) cell.setCellValue(StringUtils.join((List<String>) value, ","));
        else if (value instanceof Boolean && (field.getType().equals("custom-yes-no-child"))) {
            cell.setCellValue((boolean) value ? "Yes" : "No");
        } else if (field.getType().equals("custom-date")) {
            try {
                cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse((String) value)));
            } catch (Exception e) {
                cell.setCellValue(field.getDefaultValue().toString());
            }
        } else if (field.getType().equals("custom-country")) {
            if (Objects.isNull(value))
                cell.setCellValue("");
            else {
                if (value.toString().equals("other"))
                    cell.setCellValue("Other");
                else
                    cell.setCellValue(countries.stream().filter(c -> c.getValue().equals(field.getDefaultValue().toString())).findFirst().orElse(new CountryDTO()).getCountryName());
            }
        } else if (field.getType().equals("custom-city")) {
            if (Objects.isNull(value))
                cell.setCellValue("");
            else {
                cell.setCellValue(cities.stream().filter(c -> c.getId().equals(Long.valueOf(value.toString()))).findFirst().orElse(new CityJsonDTO()).getName());
            }
        } else if (field.getType().equals("custom-file")) {
            if (Objects.isNull(field.getFieldGroup()) || field.getFieldGroup().isEmpty()
                || (Objects.isNull(field.getFieldGroup().get(1).getDefaultValue())
                || field.getFieldGroup().get(1).getDefaultValue().toString().isEmpty()))
                cell.setCellValue("");
            else
                cell.setCellValue(apiGatewayUrl + "/general/assets/intakePrograms/"
                    + intakeProgramId + "/files/" + field.getFieldGroup().get(1).getDefaultValue().toString()
                    + "?apiKey=" + apiKey);
        } else
            cell.setCellValue(field.getDefaultValue().toString());
    }


    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping("intakePrograms/{intakeProgramId}/registrations/import")
    public ResponseEntity<Object> importApplications(@PathVariable Long intakeProgramId,
                                                     @RequestBody @NotNull MultipartFile file) throws Exception {
        if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xlsx"))
            throw new CustomRunTimeException("Expecting an Xlsx file", HttpStatus.BAD_REQUEST);
        intakeProgramService.importApplications(intakeProgramId, file);
        return ResponseWrapper.response(null, "Successfully imported");
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN})
    @PostMapping("intakePrograms/{intakeProgramId}/screening-evaluation/import")
    public ResponseEntity<Object> importScreeningEvaluations(@PathVariable Long intakeProgramId,
                                                             @RequestBody @NotNull MultipartFile file,
                                                             HttpServletRequest httpServletRequest) throws Exception {
        if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xlsx"))
            throw new CustomRunTimeException("Expecting an Xlsx file", HttpStatus.BAD_REQUEST);
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        intakeProgramService.importScreeningEvaluations(currentUserObject, intakeProgramId, file);
        return ResponseWrapper.response(null, "Successfully imported");
    }

    @GetMapping("intakePrograms/{intakeProgramId}/registrations/{registrationId}")
    public ResponseEntity<?> getRegistrationSubmission(@PathVariable Long intakeProgramId, @PathVariable Long registrationId) {
        return ResponseWrapper.response(intakeProgramService.findRegistrationSubmission(intakeProgramId, registrationId));
    }

    @GetMapping("intakePrograms/{intakeProgramId}/assessments")
    public ResponseEntity<Object> assessmentSubmissions(@RequestParam(defaultValue = "0") Integer pageNo,
                                                        @RequestParam(defaultValue = "50") Integer pageSize,
                                                        @RequestParam(defaultValue = "asc") String sortDir,
                                                        @RequestParam(defaultValue = "createdOn") String sortBy,
                                                        @RequestParam(defaultValue = "name") String filterBy,
                                                        @RequestParam(defaultValue = "") String filterKeyword,
                                                        @PathVariable Long intakeProgramId) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        Page<GetIntakeProgramSubmissionDto> list = intakeProgramService.assessmentSubmissions(intakeProgramId,
            Constant.ASSESSMENT.toString(), paging, filterKeyword, filterBy);

        return ResponseWrapper.response(list);
    }

    @GetMapping("intakePrograms/{intakeProgramId}/trend")
    public ResponseEntity<Object> submissionTrend(@PathVariable Long intakeProgramId) {
        return intakeProgramService.submissionTrend(intakeProgramId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/registrations/{registrationId}/nextPhase")
    public ResponseEntity<?> registrationNextPhase(HttpServletRequest httpServletRequest,
                                                   @PathVariable Long registrationId,
                                                   @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.registrationNextPhase(currentUserObject, intakeProgramId, registrationId);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessments/{assessmentId}/nextPhase")
    public ResponseEntity<Object> assessmentNextPhase(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long assessmentId,
                                                      @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.assessmentNextPhase(currentUserObject, intakeProgramId, assessmentId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessments/assignJudge")
    public ResponseEntity<Object> assignJudges(HttpServletRequest httpServletRequest,
                                               @PathVariable Long intakeProgramId,
                                               @Valid @RequestBody AssignJudgeDto assignJudgeDto,
                                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            Object obj = intakeProgramService.assignJudges(currentUserObject, intakeProgramId, null, assignJudgeDto);

            if (obj instanceof String) {
                return ResponseWrapper.response((String) obj, "intakeProgramId|assessmentId", HttpStatus.BAD_REQUEST);
            }

            return ResponseWrapper.response(obj);
        }

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/screening/assignEvaluators")
    public ResponseEntity<Object> assignEvaluatorsForScreening(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long intakeProgramId,
                                                               @Valid @RequestBody AssignEvaluatorsDTO assignEvaluatorsDTO,
                                                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            intakeProgramService.assignEvaluatorsForScreening(intakeProgramId, assignEvaluatorsDTO);
            return ResponseWrapper.response(null, "Evaluators successfully assigned");
        }

    }


    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessments/{assessmentId}/startEvaluation")
    public ResponseEntity<Object> startAssessmentEvaluation(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long assessmentId,
                                                            @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.startAssessmentEvaluation(currentUserObject, intakeProgramId, assessmentId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessments/{assessmentId}/stopEvaluation")
    public ResponseEntity<Object> stopAssessmentEvaluation(HttpServletRequest httpServletRequest,
                                                           @PathVariable Long assessmentId,
                                                           @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.stopAssessmentEvaluation(currentUserObject, intakeProgramId, assessmentId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId|assessmentId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);

    }

    @GetMapping("intakePrograms/assignees")
    public ResponseEntity<Object> getAssignees(@RequestParam(defaultValue = "0") Integer pageNo,
                                               @RequestParam(defaultValue = "50") Integer pageSize,
                                               @RequestParam(defaultValue = "desc") String sortDir,
                                               @RequestParam(defaultValue = "alias") String filterBy,
                                               @RequestParam(defaultValue = "") String filterKeyword,
                                               @RequestParam(defaultValue = "alias") String sortBy,
                                               @RequestParam(required = false, defaultValue = "false") boolean isForScreening) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        List<String> rln = new ArrayList<String>();
        if (isForScreening) {
            rln.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
            rln.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);
            rln.add(RoleName.ROLE_SUPER_ADMIN);
        } else {
            rln.add(RoleName.ROLE_STC_JUDGES);
            rln.add(RoleName.ROLE_NON_STC_JUDGES);
        }
        return intakeProgramService.getAssignees(rln, paging, Constant.REGISTERED.toString(), filterKeyword);
    }

    @GetMapping("intakePrograms/assignees/assessments")
    public ResponseEntity<Object> assigneesAssessements(@RequestParam(defaultValue = "0") Integer pageNo,
                                                        HttpServletRequest httpServletRequest,
                                                        @RequestParam(defaultValue = "50") Integer pageSize,
                                                        @RequestParam(defaultValue = "asc") String sortDir) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return intakeProgramService.assigneesAssessements(currentUserObject, paging);

    }

    @GetMapping("intakePrograms/assignees/bootcamps")
    public ResponseEntity<Object> assigneesBootcamps(@RequestParam(defaultValue = "0") Integer pageNo,
                                                     HttpServletRequest httpServletRequest,
                                                     @RequestParam(defaultValue = "50") Integer pageSize,
                                                     @RequestParam(defaultValue = "asc") String sortDir) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return intakeProgramService.assigneesBootcamps(currentUserObject, paging);

    }


    @GetMapping("intakePrograms/{intakeProgramId}/assessments/evaluations")
    public ResponseEntity<Object> assigneesAssessementEvaluations(@RequestParam(defaultValue = "0") Integer pageNo,
                                                                  HttpServletRequest httpServletRequest,
                                                                  @RequestParam(defaultValue = "50") Integer pageSize,
                                                                  @RequestParam(defaultValue = "asc") String sortDir,
                                                                  @RequestParam(defaultValue = "createdOn") String sortBy,
                                                                  @RequestParam(defaultValue = "name") String filterBy,
                                                                  @RequestParam(defaultValue = "") String filterKeyword,
                                                                  @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return intakeProgramService.assigneesAssessementEvaluations(currentUserObject, intakeProgramId, paging,
            filterKeyword, filterBy);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER})
    @GetMapping("intakePrograms/{intakeProgramId}/evaluation-summary/{applicationId}/{phase}")
    public ResponseEntity<Object> findEvaluatorsSummary(@PathVariable Long intakeProgramId,
                                                        @PathVariable Long applicationId,
                                                        @PathVariable String phase) {
        return ResponseWrapper.response(intakeProgramService.findEvaluatorsSummary(applicationId, phase));
    }

    @GetMapping("intakePrograms/{intakeProgramId}/screening/evaluations")
    public ResponseEntity<Object> screeningEvaluations(@RequestParam(defaultValue = "0") Integer pageNo,
                                                       HttpServletRequest httpServletRequest,
                                                       @RequestParam(defaultValue = "50") Integer pageSize,
                                                       @RequestParam(defaultValue = "asc") String sortDir,
                                                       @RequestParam(defaultValue = "createdOn") String sortBy,
                                                       @RequestParam(defaultValue = "name") String filterBy,
                                                       @RequestParam(defaultValue = "") String filterKeyword,
                                                       @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return intakeProgramService.screeningEvaluations(currentUserObject, intakeProgramId, paging,
            filterKeyword, filterBy);

    }

    @PermittedRoles(roles = {Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER, Roles.ROLE_SUPER_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/{phase}/evaluations/start")
    public ResponseEntity<JsonResponseDTO<Void>> startMultipleScreeningEvaluation(@PathVariable String phase,
                                                                                  @PathVariable Long intakeProgramId,
                                                                                  @RequestBody List<Long> applicationIds) {
        intakeProgramService.startEvaluation(phase, intakeProgramId, applicationIds);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Successfully started evaluation for the given startups"));
    }

    @PermittedRoles(roles = {Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER, Roles.ROLE_SUPER_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/{phase}/evaluations/stop")
    public ResponseEntity<JsonResponseDTO<Void>> stopMultipleScreeningEvaluation(@PathVariable String phase,
                                                                                 @PathVariable Long intakeProgramId,
                                                                                 @RequestBody List<Long> applicationIds) {
        intakeProgramService.stopEvaluation(phase, intakeProgramId, applicationIds);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Successfully stopped evaluation for the given startups"));
    }

    @PermittedRoles(roles = {Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER, Roles.ROLE_SUPER_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/{phase}/evaluations/generate-summary")
    public ResponseEntity<JsonResponseDTO<Void>> generateSummary(@PathVariable String phase,
                                                                 @PathVariable Long intakeProgramId,
                                                                 @RequestBody List<Long> applicationIds) {
        intakeProgramService.generateSummary(phase, intakeProgramId, applicationIds);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Summary generated for the given startups"));
    }

    @PermittedRoles(roles = {Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_MEMBER, Roles.ROLE_SUPER_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/{phase}/evaluations/next-phase")
    public ResponseEntity<JsonResponseDTO<Void>> moveToNextPhase(@PathVariable String phase,
                                                                 @PathVariable Long intakeProgramId,
                                                                 @RequestBody List<Long> applicationIds) {
        intakeProgramService.moveToNextPhase(phase, intakeProgramId, applicationIds);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Selected startups has moved to the next phase"));
    }

    @GetMapping("intakePrograms/{intakeProgramId}/startups")
    public ResponseEntity<?> listStartups(@RequestParam(defaultValue = "0") Integer pageNo,
                                          HttpServletRequest httpServletRequest,
                                          @RequestParam(defaultValue = "50") Integer pageSize,
                                          @RequestParam(defaultValue = "asc") String sortDir,
                                          @PathVariable Long intakeProgramId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Pageable paging = PageRequest.of(pageNo, pageSize);
        return intakeProgramService.listStartups(currentUserObject, intakeProgramId, paging);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/startups/{startupId}/inviteDueDiligence")
    public ResponseEntity<Object> inviteDueDiligence(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long startupId,
                                                     @PathVariable Long intakeProgramId,
                                                     @Valid @RequestBody InviteDueDiligenceDto inviteDueDiligenceDto,
                                                     BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            Object obj = intakeProgramService.inviteDueDiligence(currentUserObject, intakeProgramId, startupId,
                inviteDueDiligenceDto);

            if (obj instanceof String) {
                return ResponseWrapper.response((String) obj, "intakeProgramId|assessmentId", HttpStatus.BAD_REQUEST);
            }

            return ResponseWrapper.response(obj);
        }

    }

    @GetMapping("intakePrograms/{intakeProgramId}/assessments/summary")
    public ResponseEntity<Object> getAssessmentSummary(@RequestParam(defaultValue = "0") Integer pageNo,
                                                       HttpServletRequest httpServletRequest,
                                                       @RequestParam(defaultValue = "50") Integer pageSize,
                                                       @RequestParam(defaultValue = "desc") String sortDir,
                                                       @PathVariable Long intakeProgramId,
                                                       @RequestParam(defaultValue = "total") String sortBy,
                                                       @RequestParam(defaultValue = "") String filterBy,
                                                       @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return intakeProgramService.getEvalSummary(currentUserObject, Constant.ASSESSMENT_EVALUATION_COMPLETED.toString(), intakeProgramId,
            filterBy, filterKeyword, paging);
    }

    @GetMapping("intakePrograms/{intakeProgramId}/share/members")
    public ResponseEntity<?> intakeProgramsShareMembers(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        List<String> roles = new ArrayList<String>();
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);

        return intakeProgramService.intakeProgramsShareMembers(currentUserObject, intakeProgramId, roles);
    }

    @GetMapping("intakePrograms/{intakeProgramId}/shared/members")
    public ResponseEntity<?> intakeProgramsSharedMembers(HttpServletRequest httpServletRequest,
                                                         @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.intakeProgramsSharedMembers(currentUserObject, intakeProgramId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("intakePrograms/{intakeProgramId}/registration/notInNextPhase")
    public ResponseEntity<?> notSelectedForAssessment(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long intakeProgramId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.notSelectedToAssessment(currentUserObject, intakeProgramId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessments/{assessmentId}/attendance/{status}")
    public ResponseEntity<?> toggleAssessmentAttendance(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long intakeProgramId,
                                                        @PathVariable Boolean status,
                                                        @PathVariable Long assessmentId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.toggleAssessmentAttendance(currentUserObject, intakeProgramId, assessmentId,
            status);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/bootcamps/{bootcampId}/attendance/{status}")
    public ResponseEntity<?> toggleBootcampAttendance(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long intakeProgramId,
                                                      @PathVariable Boolean status,
                                                      @PathVariable Long bootcampId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.toggleBootcampAttendance(currentUserObject, intakeProgramId, bootcampId, status);
    }

    @GetMapping("intakePrograms/{intakeProgramId}/phases/{phase}/allSubmissions")
    public ResponseEntity<?> listAllsubmissions(HttpServletRequest httpServletRequest,
                                                @PathVariable Long intakeProgramId,
                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                @RequestParam(defaultValue = "50") Integer pageSize,
                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                @PathVariable String phase,
                                                @RequestParam(defaultValue = "createdOn") String sortBy,
                                                @RequestParam(defaultValue = "") String filterKeyword) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.listAllSubmissions(currentUserObject, intakeProgramId, phase, filterKeyword,
            paging);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/registrationForm/due-date/{dueDate}")
    public ResponseEntity<?> updateRegistrationFormDueDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDate,
                                                           @PathVariable Long intakeProgramId,
                                                           HttpServletRequest httpServletRequest) {
        return intakeProgramService.updateRegistrationFormDueDate((CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT),
            intakeProgramId, dueDate);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/assessment-form/publish")
    public ResponseEntity<?> publishAssessmentForm(@PathVariable Long intakeProgramId,
                                                   HttpServletRequest httpServletRequest) {
        return intakeProgramService.publishAssessmentForm((CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT),
            intakeProgramId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/bootcamp-form/publish")
    public ResponseEntity<?> publishBootcampForm(@PathVariable Long intakeProgramId,
                                                 HttpServletRequest httpServletRequest) {
        return intakeProgramService.publishBootcampForm((CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT),
            intakeProgramId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/screeningEvaluationForm/publish")
    public ResponseEntity<?> publishScreeningForm(@PathVariable Long intakeProgramId,
                                                  HttpServletRequest httpServletRequest) {
        return intakeProgramService.publishScreeningForm((CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT),
            intakeProgramId);
    }


    @GetMapping("intakePrograms/{intakeProgramId}/bootcamps")
    public ResponseEntity<Object> bootcampSubmissions(HttpServletRequest httpServletRequest,
                                                      @RequestParam(defaultValue = "0") Integer pageNo,
                                                      @RequestParam(defaultValue = "50") Integer pageSize,
                                                      @RequestParam(defaultValue = "asc") String sortDir,
                                                      @RequestParam(defaultValue = "createdOn") String sortBy,
                                                      @RequestParam(defaultValue = "name") String filterBy,
                                                      @RequestParam(defaultValue = "") String filterKeyword,
                                                      @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return intakeProgramService.bootcampSubmissions(currentUserObject, intakeProgramId,
            Constant.BOOTCAMP.toString(), paging, filterKeyword, filterBy);

    }

    @PutMapping("intakePrograms/{intakeProgramId}/bootcamps/assignJudge")
    public ResponseEntity<Object> assignBootcampJudges(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long intakeProgramId,
                                                       @Valid @RequestBody AssignJudgeDto assignJudgeDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute(CURRENT_USER_OBJECT);

            Object obj = intakeProgramService.assignBootcampJudges(currentUserObject, intakeProgramId, null,
                assignJudgeDto);

            if (obj instanceof String) {
                return ResponseWrapper.response((String) obj, "intakeProgramId|assessmentId", HttpStatus.BAD_REQUEST);
            }

            return ResponseWrapper.response(obj);
        }

    }

    @GetMapping("intakePrograms/{intakeProgramId}/bootcamps/evaluations")
    public ResponseEntity<Object> assigneesBootcampsEvaluations(@RequestParam(defaultValue = "0") Integer pageNo,
                                                                HttpServletRequest httpServletRequest,
                                                                @RequestParam(defaultValue = "50") Integer pageSize,
                                                                @RequestParam(defaultValue = "desc") String sortDir,
                                                                @RequestParam(defaultValue = "createdOn") String sortBy,
                                                                @RequestParam(defaultValue = "email") String filterBy,
                                                                @RequestParam(defaultValue = "") String filterKeyword,
                                                                @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return intakeProgramService.assigneesBootcampEvaluations(currentUserObject, intakeProgramId, paging,
            filterKeyword, filterBy);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/bootcamps/{bootcampId}/startEvaluation")
    public ResponseEntity<Object> startBootcampEvaluation(HttpServletRequest httpServletRequest,
                                                          @PathVariable Long bootcampId,
                                                          @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return intakeProgramService.startBootcampEvaluation(currentUserObject, intakeProgramId, bootcampId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("intakePrograms/{intakeProgramId}/bootcamps/{bootcampId}/stopEvaluation")
    public ResponseEntity<Object> stopBootcampEvaluation(HttpServletRequest httpServletRequest,
                                                         @PathVariable Long bootcampId,
                                                         @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.stopBootcampEvaluation(currentUserObject, intakeProgramId, bootcampId);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId|bootcampId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);

    }

    @PutMapping("intakePrograms/{intakeProgramId}/bootcamps/finish")
    public ResponseEntity<Object> finitBootcamp(HttpServletRequest httpServletRequest,
                                                @PathVariable Long intakeProgramId,
                                                @Valid @RequestBody BootCampSelectedStartupDto selectedStartupRequest,
                                                BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = intakeProgramService.finitBootcamp(currentUserObject, intakeProgramId, selectedStartupRequest);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "intakeProgramId|assessmentId", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);

    }

    @GetMapping("intakePrograms/{intakeProgramId}/bootcamps/summary")
    public ResponseEntity<Object> getBootcampSummary(@RequestParam(defaultValue = "0") Integer pageNo,
                                                     HttpServletRequest httpServletRequest,
                                                     @RequestParam(defaultValue = "50") Integer pageSize,
                                                     @RequestParam(defaultValue = "asc") String sortDir,
                                                     @PathVariable Long intakeProgramId,
                                                     @RequestParam(defaultValue = "total") String sortBy,
                                                     @RequestParam(defaultValue = "") String filterBy,
                                                     @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return intakeProgramService.getEvalSummary(currentUserObject, Constant.BOOTCAMP_EVALUATION_COMPLETED.toString(), intakeProgramId,
            filterBy, filterKeyword, paging);
    }

    @PostMapping("intakePrograms/{intakeProgramId}/assessment/notInNextPhase")
    public ResponseEntity<?> notSelectedForBootCamp(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long intakeProgramId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return intakeProgramService.notSelectedToBootCamp(currentUserObject, intakeProgramId);
    }

    @PutMapping("intakePrograms/{intakeProgramId}/registrations/{registrationId}/screening/start")
    public ResponseEntity<?> startScreeningEvaluation(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long intakeProgramId,
                                                      @PathVariable Long registrationId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return intakeProgramService.startEvaluation(currentUserObject, intakeProgramId, registrationId);
    }

    @PutMapping("intakePrograms/{intakeProgramId}/registrations/{registrationId}/screening/stop")
    public ResponseEntity<?> stopScreeningEvaluation(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long intakeProgramId,
                                                     @PathVariable Long registrationId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return intakeProgramService.stopEvaluation(currentUserObject, intakeProgramId, registrationId);
    }

    @PutMapping("intakePrograms/{intakeProgramId}/registrations/{registrationId}/screening/submit")
    public ResponseEntity<?> submitScreeningEvaluation(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long intakeProgramId,
                                                       @PathVariable Long registrationId,
                                                       @Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        evaluationSubmissionDto.setPhase("SCREENING");
        return ResponseWrapper.response(evaluationSummaryService.screeningEvaluationSubmission(currentUserObject,
            evaluationSubmissionDto), "Evaluation submitted successfully");
    }

    @GetMapping("intakePrograms/{intakeProgramId}/screening/summary")
    public ResponseEntity<Object> screeningSummary(@RequestParam(defaultValue = "0") Integer pageNo,
                                                   HttpServletRequest httpServletRequest,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @RequestParam(defaultValue = "desc") String sortDir,
                                                   @PathVariable Long intakeProgramId,
                                                   @RequestParam(defaultValue = "total") String sortBy,
                                                   @RequestParam(defaultValue = "") String filterBy,
                                                   @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return intakeProgramService.getScreeningSummary(currentUserObject, intakeProgramId,
            filterBy, filterKeyword, paging);
    }
}
