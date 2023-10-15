package com.stc.inspireu.controllers;

import com.stc.inspireu.authorization.PermittedRoles;
import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostDueDiligenceTemplate2021Dto;
import com.stc.inspireu.dtos.PutDueDiligenceTemplate2021ManagementDto;
import com.stc.inspireu.services.DueDiligenceService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class DueDiligenceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Utility utility;
    private final DueDiligenceService dueDiligenceService;

    @PutMapping("startups/_dueDiligences/clone")
    public ResponseEntity<Object> _cloneDueDiligence(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> data = dueDiligenceService.cloneDueDiligence(currentUserObject);

        return ResponseWrapper.response(data);
    }

    @GetMapping("startups/_dueDiligences/{dueDiligenceId}/fields")
    public ResponseEntity<?> _getDueDiligenceFields(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return dueDiligenceService.getDueDiligenceFields(currentUserObject, dueDiligenceId);

    }

    @GetMapping("startups/_dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents")
    public ResponseEntity<Object> _getDueDiligenceDocuments(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long dueDiligenceId,
                                                            @PathVariable String fieldId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<?> ls = dueDiligenceService.getDueDiligenceDocuments(currentUserObject, dueDiligenceId, fieldId);

        return ResponseWrapper.response(ls);
    }

    @PostMapping(value = "startups/_dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> _createDueDiligence(HttpServletRequest httpServletRequest,
                                                      @RequestParam("multipartFiles") MultipartFile[] multipartFiles,
                                                      @PathVariable Long dueDiligenceId,
                                                      @PathVariable String fieldId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> fileCheck = utility.checkFile(multipartFiles);

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        if (multipartFiles.length == 0) {
            return ResponseWrapper.response("required atleast one file", "multipartFiles", HttpStatus.BAD_REQUEST);
        } else if (multipartFiles.length > 5) {
            return ResponseWrapper.response("at a time 5 file maximum", "multipartFiles", HttpStatus.BAD_REQUEST);
        } else {
            Object data = dueDiligenceService.createDueDiligence(currentUserObject, multipartFiles, dueDiligenceId,
                fieldId);
            if (data != null) {
                return ResponseWrapper.response(null, "files uploaded");
            } else {
                return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }

    @DeleteMapping("startups/_dueDiligences/{dueDiligenceId}/fields/{fieldId}/documents/{documentId}")
    public ResponseEntity<Object> _deleteDueDiligence(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long dueDiligenceId,
                                                      @PathVariable String fieldId,
                                                      @PathVariable Long documentId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Integer data = dueDiligenceService.deleteDueDiligence(currentUserObject, dueDiligenceId, fieldId,
            documentId);

        if (data != null) {
            return ResponseWrapper.response(null, "document removed");
        } else {
            return ResponseWrapper.response("invaid documentId", "documentId", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("startups/_dueDiligences/{dueDiligenceId}/fields/{fieldId}/notes")
    public ResponseEntity<?> getDueDiligenceFileNotes(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long dueDiligenceId,
                                                      @PathVariable String fieldId,
                                                      @PageableDefault(sort = {
                                                          "createdOn"}, page = 0, size = 50, direction = Sort.Direction.ASC) Pageable pageable) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return dueDiligenceService.getDueDiligenceFileNotes(currentUserObject, fieldId, pageable);

    }

    @PostMapping(value = "startups/_dueDiligences/{dueDiligenceId}/fields/{fieldId}/notes")
    public ResponseEntity<Object> createDueDiligenceFileNotes(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long dueDiligenceId,
                                                              @PathVariable String fieldId,
                                                              @RequestBody String note,
                                                              BindingResult bindingResult) {

        if (note.equals("")) {
            return ResponseWrapper.response400("note required", "note");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.createDueDiligenceFileNote(currentUserObject, fieldId, note);

        if (data != null) {
            return ResponseWrapper.response(null, "notes created");
        } else {
            return ResponseWrapper.response400((String) data, "");
        }

    }

    @PutMapping(value = "startups/_dueDiligences/{dueDiligenceId}/upload/confirm")
    public ResponseEntity<Object> confirmDueDiligenceUpload(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.confirmDueDiligenceUpload(currentUserObject, dueDiligenceId);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "DueDiligence uploaded");
        } else {

            return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/{dueDiligenceId}")
    public ResponseEntity<?> _getDueDiligenceDetail(HttpServletRequest httpServletRequest,
                                                    @RequestParam(defaultValue = "false") String isRealId,
                                                    @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return dueDiligenceService.getDueDiligenceDetail(currentUserObject, dueDiligenceId, isRealId);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/intakeProgram/{intakeProgramName}")
    public ResponseEntity<Object> _getDueDiligences(HttpServletRequest httpServletRequest,
                                                    @RequestParam(defaultValue = "0") Integer pageNo,
                                                    @RequestParam(defaultValue = "100") Integer pageSize,
                                                    @RequestParam(defaultValue = "asc") String sortDir,
                                                    @PathVariable String intakeProgramName) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("modifiedOn").ascending());
        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("modifiedOn").descending());
        }
        Object ls = dueDiligenceService.getDueDiligences(currentUserObject, intakeProgramName, paging);

        return ResponseWrapper.response(ls);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/{dueDiligenceId}/submissions")
    public ResponseEntity<?> _getDueDiligenceSubmissions(HttpServletRequest httpServletRequest,
                                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                                         @RequestParam(defaultValue = "100") Integer pageSize,
                                                         @RequestParam(defaultValue = "asc") String sortDir,
                                                         @PathVariable Long dueDiligenceId,
                                                         @RequestParam(defaultValue = "all") String filterBy,
                                                         @RequestParam(defaultValue = "") String filterKeyword,
                                                         @RequestParam(defaultValue = "modifiedOn") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return dueDiligenceService.getDueDiligenceSubmissions(currentUserObject, dueDiligenceId, filterBy,
            filterKeyword, paging);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("management/_dueDiligences/{dueDiligenceId}/publish")
    public ResponseEntity<Object> _publishDueDiligence(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.publishDueDiligenceTemplate(currentUserObject, dueDiligenceId);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "Due Diligence published");
        } else {

            return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @DeleteMapping("management/_dueDiligences/{dueDiligenceId}")
    public ResponseEntity<Object> _deleteDueDiligence(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.deleteDueDiligenceTemplate(currentUserObject, dueDiligenceId);
        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "Due Diligence deleted");
        } else {

            return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/review")
    public ResponseEntity<?> _reviewDueDiligence(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long dueDiligenceId,
                                                 @PathVariable Long startupId,
                                                 @Valid @RequestBody PutDueDiligenceTemplate2021ManagementDto ddReviewRequest, BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return dueDiligenceService.reviewStartupDueDiligence(currentUserObject, dueDiligenceId, startupId,
            ddReviewRequest);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields")
    public ResponseEntity<Object> _getManagementDueDiligenceFields(HttpServletRequest httpServletRequest,
                                                                   @PathVariable Long startupId,
                                                                   @PathVariable Long dueDiligenceId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> ls = dueDiligenceService.getManagementDueDiligenceFields(currentUserObject,
            dueDiligenceId, startupId);

        return ResponseWrapper.response(ls);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields/{fieldId}/documents")
    public ResponseEntity<Object> _getManagementDueDiligenceDocuments(HttpServletRequest httpServletRequest,
                                                                      @PathVariable Long startupId,
                                                                      @PathVariable Long dueDiligenceId, @PathVariable String fieldId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<?> ls = dueDiligenceService.getManagementDueDiligenceDocuments(currentUserObject, dueDiligenceId,
            startupId, fieldId);

        return ResponseWrapper.response(ls);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("management/_dueDiligences/{dueDiligenceId}")
    public ResponseEntity<Object> _updateDueDiligenceTemplate(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long dueDiligenceId,
                                                              @Valid @RequestBody PostDueDiligenceTemplate2021Dto dueDiligenceRequest,
                                                              BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.updateDueDiligenceTemplate(currentUserObject, dueDiligenceId,
            dueDiligenceRequest);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "Due diligence update");
        }

        return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields/{fieldId}/review")
    public ResponseEntity<?> _reviewDueDiligenceFields(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long dueDiligenceId,
                                                       @PathVariable Long startupId,
                                                       @PathVariable String fieldId,
                                                       @Valid @RequestBody PutDueDiligenceTemplate2021ManagementDto ddReviewRequest,
                                                       BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return dueDiligenceService.reviewStartupDueDiligenceFields(currentUserObject, dueDiligenceId, startupId,
            fieldId, ddReviewRequest);
    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @GetMapping("management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields/{fieldId}/notes")
    public ResponseEntity<Object> getManagementDueDiligenceFileNotes(HttpServletRequest httpServletRequest,
                                                                     @PathVariable Long dueDiligenceId,
                                                                     @PathVariable Long startupId,
                                                                     @PathVariable String fieldId,
                                                                     @PageableDefault(sort = {"createdOn"},
                                                                         page = 0, size = 50, direction = Sort.Direction.ASC)
                                                                     Pageable pageable) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.getManagementDueDiligenceFileNotes(currentUserObject, dueDiligenceId,
            startupId, fieldId, pageable);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceFileNoteId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(data);
        }
        return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @PermittedRoles(roles = {Roles.ROLE_SUPER_ADMIN, Roles.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping(value = "management/_dueDiligences/{dueDiligenceId}/startups/{startupId}/fields/{fieldId}/notes")
    public ResponseEntity<Object> createManagementDueDiligenceFileNotes(HttpServletRequest httpServletRequest,
                                                                        @PathVariable Long dueDiligenceId,
                                                                        @PathVariable Long startupId,
                                                                        @PathVariable String fieldId,
                                                                        @RequestBody String note, BindingResult bindingResult) {

        if (note.isEmpty()) {
            return ResponseWrapper.response400("note required", "note");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = dueDiligenceService.createManagementDueDiligenceFileNote(currentUserObject, dueDiligenceId,
            startupId, fieldId, note);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "dueDiligenceFileNoteId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "notes created");
        }
        return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
