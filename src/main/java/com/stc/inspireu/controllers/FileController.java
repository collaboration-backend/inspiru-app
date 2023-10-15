package com.stc.inspireu.controllers;

import com.stc.inspireu.authorization.PermittedRoles;
import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostFileSettingDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.File;
import com.stc.inspireu.services.FileService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Utility utility;
    private final FileService fileService;

    @PostMapping(value = "management/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createManagmentGeneralFile(HttpServletRequest httpServletRequest,
                                                             @RequestParam(value = "file") MultipartFile multipartFile,
                                                             @RequestParam Long inTakePgmId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> fileCheck = utility.checkFile(multipartFile);

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        if (multipartFile != null) {
            File fl = fileService.createManagmentGeneralFile(currentUserObject, multipartFile, inTakePgmId,
                Constant.DRAFT.toString());

            if (fl != null) {
                Map<String, Object> data = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("fileName", fl.getName());
                        if (Objects.nonNull(fl.getCreatedOn()))
                            put("createAt", ZonedDateTime.of(fl.getCreatedOn(), ZoneId.systemDefault()).toInstant().toEpochMilli());
                        put("createUserId", fl.getCreatedUser().getId());
                        put("createUserEmail", fl.getCreatedUser().getEmail());
                        put("createUserName", fl.getCreatedUser().getAlias());
                    }
                };

                return ResponseWrapper.response(data);
            } else {
                return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return ResponseWrapper.response("file required", "file", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("management/files")
    public ResponseEntity<Object> listManagementFiles(@RequestParam(defaultValue = "0") Integer pageNo,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(defaultValue = "createdOn") String sortBy,
                                                      @RequestParam(defaultValue = "") String filterBy,
                                                      @RequestParam(defaultValue = "asc") String sortDir,
                                                      @RequestParam(defaultValue = "") String filterKeyword,
                                                      HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return fileService.listManagementFiles(currentUserObject, filterKeyword, filterBy, paging);
    }

    @GetMapping("startups/files")
    public ResponseEntity<Object> getFiles(@RequestParam(defaultValue = "0") Integer pageNo,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(defaultValue = "createdOn") String sortBy,
                                           @RequestParam(defaultValue = "") String filterBy,
                                           @RequestParam(defaultValue = "asc") String sortDir,
                                           @RequestParam(defaultValue = "") String filterKeyword,
                                           HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return fileService.getFiles(currentUserObject, filterKeyword, paging);
    }

    @GetMapping("management/files/{fileId}")
    public void getFilesById(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             @PathVariable Long fileId) throws IOException {

        Map<String, Object> data = fileService.getManagementFileAsset(fileId);

        if (data != null) {
            java.io.File file = (java.io.File) data.get("file");
            InputStream in = new FileInputStream(file);
            httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName());

            StreamUtils.copy(in, httpServletResponse.getOutputStream());
        } else {
            ResponseWrapper.createResponse404(httpServletResponse);
        }
    }

    @DeleteMapping("management/files/{fileId}")
    public ResponseEntity<Object> deleteFilesById(@PathVariable Long fileId) {
        return ResponseWrapper.response(fileService.deleteManagmentFile(fileId) ? "file deleted" : "failed");
    }

    @PutMapping("management/files/{fileId}/publish")
    public ResponseEntity<?> updateFileStatus(@PathVariable Long fileId) {

        return fileService.updateFileStatus(fileId, Constant.PUBLISHED.toString());
    }

    @PutMapping("management/files/{fileId}/draft")
    public ResponseEntity<?> updateFileDraft(@PathVariable Long fileId) {

        return fileService.updateFileStatus(fileId, Constant.DRAFT.toString());
    }

    @PermittedRoles(roles = {Roles.ROLE_MANAGEMENT_TEAM_ADMIN, Roles.ROLE_SUPER_ADMIN})
    @PostMapping("management/files/settings")
    public ResponseEntity<Object> saveFileSettings(HttpServletRequest httpServletRequest,
                                                   @Valid @RequestBody PostFileSettingDto postFileSettingDto,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return fileService.saveFileSettings(currentUserObject, postFileSettingDto);
    }

    @GetMapping("management/files/settings")
    public ResponseEntity<?> getFileSettings(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return fileService.getFileSettings(currentUserObject);
    }
}
