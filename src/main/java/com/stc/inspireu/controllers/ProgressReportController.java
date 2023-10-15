package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostManagementProgressReportDto;
import com.stc.inspireu.dtos.PostProgressReportDto;
import com.stc.inspireu.dtos.ProgressReportDto;
import com.stc.inspireu.dtos.PutProgressReportDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.jpa.projections.ProjectProgressReportFile;
import com.stc.inspireu.services.ProgessReportService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/${api.version}")
@RequiredArgsConstructor
public class ProgressReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProgessReportService progessReportService;

    private final Utility utility;

    @PutMapping("startups/progressReports/available/notifyManagement")
    public ResponseEntity<?> notifyManagement(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return progessReportService.notifyManagement(currentUserObject);

    }

    @GetMapping("startups/progressReports/available")
    public ResponseEntity<?> checkProgressReports(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return progessReportService.checkProgressReports(currentUserObject);

    }

    @GetMapping("startups/progressReports")
    public ResponseEntity<Object> getProgressReports(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<ProgressReportDto> ls = progessReportService.getProgressReports(currentUserObject);

        return ResponseWrapper.response(ls);

    }

    @PostMapping(value = "startups/progressReports")
    public ResponseEntity<Object> createProgressReports(HttpServletRequest httpServletRequest,
                                                        @Valid @RequestBody PostProgressReportDto postProgressReportDto,
                                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = progessReportService.createProgessReportMonthly(currentUserObject, postProgressReportDto);

        if (data != null) {
            return ResponseWrapper.response(data, "progress report created");
        } else {
            return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("startups/progressReports/{progressReportId}")
    public ResponseEntity<Object> getProgressReport(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long progressReportId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        ProgressReportDto pr = progessReportService.getProgressReport(currentUserObject, progressReportId);

        if (pr != null) {
            return ResponseWrapper.response(pr);
        } else {
            return ResponseWrapper.response(progressReportId + " not found", "progressReportId", HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping(value = "startups/progressReports/{progressReportId}")
    public ResponseEntity<Object> updateProgressReports(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long progressReportId,
                                                        @Valid @RequestBody PutProgressReportDto putProgressReportDto,
                                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");
            Object data = progessReportService.updateProgessReportMonthly(currentUserObject, putProgressReportDto,
                progressReportId);
            return ResponseWrapper.response(data, "progress report updated");
        }

    }

    @GetMapping("startups/progressReports/{progressReportId}/files")
    public ResponseEntity<Object> getProgressReportFiles(HttpServletRequest httpServletRequest,
                                                         @PathVariable Long progressReportId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<ProjectProgressReportFile> ls = progessReportService.getProgressReportFiles(currentUserObject,
            progressReportId);

        return ResponseWrapper.response(ls);

    }

    @GetMapping("management/progressReports")
    public ResponseEntity<?> getManagementProgressReports(HttpServletRequest httpServletRequest,
                                                          @RequestParam(defaultValue = "0") Integer pageNo,
                                                          @RequestParam(defaultValue = "50") Integer pageSize,
                                                          @RequestParam(defaultValue = "desc") String sortDir,
                                                          @RequestParam(defaultValue = "all") String filterBy,
                                                          @RequestParam(defaultValue = "all") String searchBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("updatedAt").descending());

        if (sortDir.equals("asc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("updatedAt").ascending());
        }

        return progessReportService.getManagementProgressReports(currentUserObject, paging);

    }

    @PostMapping("management/progressReports/draft")
    public ResponseEntity<?> draftManagementProgressReport(HttpServletRequest httpServletRequest,
                                                           @RequestBody PostManagementProgressReportDto progressReportMngtDto,
                                                           BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return progessReportService.createManagementProgressReport(currentUserObject, progressReportMngtDto,
                Constant.DRAFT.toString());
        }
    }

    @PutMapping("management/progressReports/{progressReportId}")
    public ResponseEntity<?> updateManagementProgressReport(HttpServletRequest httpServletRequest,
                                                            @RequestBody PostManagementProgressReportDto progressReportMngtDto,
                                                            BindingResult bindingResult,
                                                            @PathVariable Long progressReportId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return progessReportService.updateManagementProgressReport(currentUserObject, progressReportMngtDto,
                progressReportId);
        }
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("management/progressReports/publish")
    public ResponseEntity<?> publishManagementProgressReport(HttpServletRequest httpServletRequest,
                                                             @RequestBody PostManagementProgressReportDto progressReportMngtDto,
                                                             BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return progessReportService.createManagementProgressReport(currentUserObject, progressReportMngtDto,
                Constant.PUBLISHED.toString());
        }
    }

    @DeleteMapping("management/progressReports/{progressReportId}")
    public ResponseEntity<?> deleteManagementProgressReport(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long progressReportId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return progessReportService.deleteManagementProgressReportById(currentUserObject, progressReportId);

    }

    @GetMapping("management/progressReports/{progressReportId}")
    public ResponseEntity<?> getManagementProgressReportById(HttpServletRequest httpServletRequest,
                                                             @PathVariable Long progressReportId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return progessReportService.getManagementProgressReportById(currentUserObject, progressReportId);

    }

    @PutMapping("management/progressReports/{progressReportId}/status/{isPublished}")
    public ResponseEntity<?> updateManagementProgressReportStatus(HttpServletRequest httpServletRequest,
                                                                  @PathVariable Long progressReportId,
                                                                  @PathVariable Boolean isPublished) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return progessReportService.updateManagementProgressReportStatus(currentUserObject,
            Boolean.TRUE.equals(isPublished) ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString(), progressReportId);

    }

    @GetMapping("management/intakePrograms/{intakeProgramId}/progressReports/submissions")
    public ResponseEntity<Object> getProgressReportMonthlySubmissions(HttpServletRequest httpServletRequest,
                                                                      @PathVariable Long intakeProgramId,
                                                                      @RequestParam(defaultValue = "0") Integer pageNo,
                                                                      @RequestParam(defaultValue = "50") Integer pageSize,
                                                                      @RequestParam(defaultValue = "desc") String sortDir,
                                                                      @RequestParam(defaultValue = "all") String filterBy,
                                                                      @RequestParam(defaultValue = "") String filterKeyword,
                                                                      @RequestParam(defaultValue = "createdOn") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());

        if (sortDir.equals("asc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        }

        Page<ProgressReportDto> list = progessReportService.getProgressReportMonthlySubmissions(currentUserObject,
            intakeProgramId, filterKeyword, filterBy, paging);

        return ResponseWrapper.response(list);

    }

    @PutMapping(value = "startups/_progressReports/{progressReportId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> putProgressReports(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long progressReportId,
                                                     @Valid PutProgressReportDto putProgressReportDto,
                                                     BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Map<String, Object> fileCheck = utility.checkFile(putProgressReportDto.getFiles());

            if (!(boolean) fileCheck.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");
            Object data = progessReportService.putProgressReports(currentUserObject, putProgressReportDto,
                progressReportId);
            return ResponseWrapper.response(data, "progress report updated");
        }

    }
}
