package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CreateFileFolderDto;
import com.stc.inspireu.dtos.ShareFileFolderDto;
import com.stc.inspireu.services.FileFolderService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}")
public class FileFolderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CURRENT_USER_OBJECT = "currentUserObject";
    private final FileFolderService fileFolderService;
    private final Utility utility;

    @Authorize
    @GetMapping("management/fileFolders/intakePrograms")
    public ResponseEntity<?> intakeProgramsFileFolders(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.intakeProgramsFileFolders(currentUserObject);
    }

    @Authorize
    @GetMapping("management/fileFolders/{fileFolderId}/shared/members")
    public ResponseEntity<?> sharedMembersFileFolders(HttpServletRequest httpServletRequest, @PathVariable String fileFolderId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.sharedMembersFileFolders(currentUserObject, fileFolderId);
    }

    @Authorize
    @DeleteMapping("management/fileFolders/{fileFolderId}/shared/members/{memberId}")
    public ResponseEntity<?> unshareMemberFileFolders(HttpServletRequest httpServletRequest,
                                                      @PathVariable String fileFolderId,
                                                      @PathVariable Long memberId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.unshareMemberFileFolders(currentUserObject, fileFolderId, memberId);
    }

    @Authorize
    @GetMapping("management/fileFolders/{fileFolderId}/share/members")
    public ResponseEntity<?> shareMembersFileFolders(HttpServletRequest httpServletRequest, @PathVariable String fileFolderId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        List<String> roles = new ArrayList<String>();
        roles.add(RoleName.ROLE_SUPER_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);

        return fileFolderService.shareMembersFileFolders(currentUserObject, fileFolderId, roles);
    }

    @Authorize
    @PostMapping("management/fileFolders/{fileFolderId}/share/members")
    public ResponseEntity<?> sharingMembersFileFolders(HttpServletRequest httpServletRequest,
                                                       @PathVariable String fileFolderId,
                                                       @Valid @RequestBody ShareFileFolderDto shareFileFolderDto,
                                                       BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return fileFolderService.sharingMembersFileFolders(currentUserObject, fileFolderId, shareFileFolderDto);
    }

    @Authorize
    @GetMapping("management/fileFolders")
    public ResponseEntity<?> listFileFolders(@RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "1000") Integer pageSize,
                                             @RequestParam(defaultValue = "createdOn") String sortBy,
                                             @RequestParam(defaultValue = "") String filterBy,
                                             @RequestParam(defaultValue = "") String filterKeyword,
                                             @RequestParam(defaultValue = "desc") String sortDir,
                                             @RequestParam(defaultValue = "") String name,
                                             @RequestParam(defaultValue = "0") String parentFolderId,
                                             @RequestParam(defaultValue = "0") String rootFolderId,
                                             HttpServletRequest httpServletRequest) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.listFileFolders(currentUserObject, rootFolderId, parentFolderId, filterBy,
            filterKeyword, paging);
    }

    @Authorize
    @GetMapping("management/fileFolders/{fileFolderId}")
    public ResponseEntity<?> getFileFolder(HttpServletRequest httpServletRequest, @PathVariable String fileFolderId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return fileFolderService.getFileFolder(currentUserObject, fileFolderId);
    }

    @Authorize
    @PostMapping(value = "management/fileFolders", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postFileFolder(HttpServletRequest httpServletRequest,
                                            @Valid @ModelAttribute CreateFileFolderDto createFileFolderDto,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Map<String, Object> fileCheck = utility.checkFile(createFileFolderDto.getFiles());

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.postFileFolder(currentUserObject, createFileFolderDto);
    }

    @Authorize
    @PutMapping("management/fileFolders/{fileFolderId}")
    public ResponseEntity<?> putFileFolder(HttpServletRequest httpServletRequest,
                                           @PathVariable String fileFolderId,
                                           @Valid @ModelAttribute CreateFileFolderDto createFileFolderDto,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return fileFolderService.putFileFolder(currentUserObject, fileFolderId, createFileFolderDto);
    }

    @Authorize
    @PutMapping("management/fileFolders/{fileFolderId}/toggleStatus")
    public ResponseEntity<?> toggleStatus(HttpServletRequest httpServletRequest, @PathVariable String fileFolderId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return fileFolderService.toggleStatus(currentUserObject, fileFolderId);
    }

    @Authorize
    @DeleteMapping("management/fileFolders/{fileFolderId}")
    public ResponseEntity<?> deleteFileFolder(HttpServletRequest httpServletRequest, @PathVariable String fileFolderId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        return fileFolderService.deleteFileFolder(currentUserObject, fileFolderId);
    }

    @Authorize
    @GetMapping("startups/fileFolders")
    public ResponseEntity<?> listStartupsFileFolders(@RequestParam(defaultValue = "0") Integer pageNo,
                                                     @RequestParam(defaultValue = "1000") Integer pageSize,
                                                     @RequestParam(defaultValue = "createdOn") String sortBy,
                                                     @RequestParam(defaultValue = "") String filterBy,
                                                     @RequestParam(defaultValue = "") String filterKeyword,
                                                     @RequestParam(defaultValue = "desc") String sortDir,
                                                     @RequestParam(defaultValue = "") String name,
                                                     @RequestParam(defaultValue = "0") String parentFolderId,
                                                     @RequestParam(defaultValue = "0") String rootFolderId,
                                                     HttpServletRequest httpServletRequest) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return fileFolderService.listStartupsFileFolders(currentUserObject, rootFolderId, parentFolderId, filterBy,
            filterKeyword, paging);
    }

}
