package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentPermissionObject;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.services.AcademyRoomService;
import com.stc.inspireu.services.ResourcePermissionService;
import com.stc.inspireu.utils.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.*;

@RestController
@RequestMapping("/api/${api.version}")
@RequiredArgsConstructor
public class ManagementAcademyRoomController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final ResourcePermissionService resourcePermissionService;

    private final AcademyRoomService academyRoomService;

    private final Utility utility;

    @Authorize
    @GetMapping("management/academyRooms/intakePrograms")
    public ResponseEntity<Object> getacademyProgramList() {
        return ResponseWrapper.response(ConstantUtility.getacademyProgramList());
    }

    @Authorize
    @GetMapping("management/academyRooms/intakePrograms/{intakeProgramName}")
    public ResponseEntity<Object> getacademyProgramIntakeList(HttpServletRequest httpServletRequest,
                                                              @PathVariable String intakeProgramName) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        List<Object> list = academyRoomService.dropdownManagementAcademyRoomIntakes(currentUserObject,
            intakeProgramName);
        return ResponseWrapper.response(list);
    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @GetMapping("management/academyRooms/{academyRoomId}")
    public ResponseEntity<Object> getManagementAcademyRoom(HttpServletRequest httpServletRequest,
                                                           @PathVariable Long academyRoomId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object obj = academyRoomService.getManagementAcademyRoom(currentUserObject, academyRoomId);
        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "academyRoomId", HttpStatus.BAD_REQUEST);
        }
        return ResponseWrapper.response(obj);
    }

    @Authorize // special case handle inside controller
    @GetMapping("management/academyRooms/status/{academyRoomStatus}")
    public ResponseEntity<Object> getManagementAcademyRoomsByAcademyRoomStatus(HttpServletRequest httpServletRequest,
                                                                               @PathVariable String academyRoomStatus,
                                                                               @RequestParam(defaultValue = "0") Integer pageNo,
                                                                               @RequestParam(defaultValue = "500") Integer pageSize,
                                                                               @RequestParam(defaultValue = "asc") String sortDir,
                                                                               @RequestParam(defaultValue = "all") String filterBy,
                                                                               @RequestParam(defaultValue = "all") String filterByValue,
                                                                               @RequestParam(defaultValue = "all") String searchBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        List<AcademyRoomManagementDto> list = Collections.emptyList();

        Set<Long> academyRoomIds = resourcePermissionService.getManagementAcademyRoomIds(currentUserObject.getUserId());

        list = academyRoomService.getManagementAcademyRoomsByAcademyRoomStatus(currentUserObject, academyRoomStatus,
            paging, academyRoomIds);

        return ResponseWrapper.response(list);

    }

    @Authorize(roles = {RoleName.ROLE_COACHES_AND_TRAINERS, RoleName.ROLE_SUPER_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @PostMapping(value = "management/academyRooms")
    public ResponseEntity<?> createAcademyRooms(HttpServletRequest httpServletRequest,
                                                @Valid @RequestBody PostAcademyRoomDto academyRoomRequest,
                                                BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.createAcademyRoom(currentUserObject, academyRoomRequest);
    }

    @Authorize(roles = {RoleName.ROLE_COACHES_AND_TRAINERS, RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @GetMapping(value = "management/academyRooms/{intakeProgramId}/{startUpId}")
    public ResponseEntity<?> getAcademicRoomByStartup(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long intakeProgramId,
                                                      @PathVariable Long startUpId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.getAcademyRoomForStartups(currentUserObject, intakeProgramId, startUpId);
    }

    @Authorize(roles = {RoleName.ROLE_COACHES_AND_TRAINERS, RoleName.ROLE_SUPER_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @GetMapping(value = "management/academyRooms/{academicRoomId}/startup/{startUpId}/workshopSessions")
    public ResponseEntity<?> getWorkShopSessionByStartup(HttpServletRequest httpServletRequest,
                                                         @PathVariable Long academicRoomId,
                                                         @PathVariable Long startUpId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.getWorkshopSessionForStartups(currentUserObject, academicRoomId, startUpId);
    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.delete, resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @DeleteMapping("management/academyRooms/{academyRoomId}")
    public ResponseEntity<Object> deleteAcademyRoom(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.deleteAcademyRoom(currentUserObject, academyRoomId);
        if (data instanceof String) {
            return ResponseWrapper.response400((String) data, "academyRoomId");
        }
        return ResponseWrapper.response(null, "academyRoom deleted " + academyRoomId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN},
        permission = PermissionUtil.edit, resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @PutMapping(value = "management/academyRooms/{academyRoomId}/publish")
    public ResponseEntity<Object> publishAcademyRooms(HttpServletRequest httpServletRequest, @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.publishAcademyRoom(currentUserObject, academyRoomId);
        if (data != null) {
            return ResponseWrapper.response(null, "academy room published");
        }
        return ResponseWrapper.response400("invalid academyRoomId", "academyRoomId");

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN},
        permission = PermissionUtil.edit, resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @PutMapping(value = "management/academyRooms/{academyRoomId}/share")
    public ResponseEntity<?> shareAcademyRooms(HttpServletRequest httpServletRequest,
                                               @PathVariable Long academyRoomId,
                                               @Valid @RequestBody List<PutAcademyRoomShareDto> putAcademyRoomShareDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.shareAcademyRoom(currentUserObject, academyRoomId, putAcademyRoomShareDto);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN},
        permission = PermissionUtil.edit, resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @PutMapping(value = "management/academyRooms/{academyRoomId}/unShare")
    public ResponseEntity<Object> unShareAcademyRooms(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long academyRoomId,
                                                      @Valid @RequestBody PutAcademyRoomShareDto putAcademyRoomShareDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.unShareAcademyRoom(currentUserObject, academyRoomId, putAcademyRoomShareDto);
        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "academyRoomId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "academy room sharing removed");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {RoleName.ROLE_COACHES_AND_TRAINERS, RoleName.ROLE_SUPER_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @GetMapping("management/academyRooms/{academyRoomId}/share/members")
    public ResponseEntity<Object> dropdownManagementShareMembers(HttpServletRequest httpServletRequest,
                                                                 @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        List<String> roles = new ArrayList<String>();
        roles.add(RoleName.ROLE_COACHES_AND_TRAINERS);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);
        List<Object> list = academyRoomService.dropdownManagementShareMembers(currentUserObject, academyRoomId, roles);
        return ResponseWrapper.response(list);
    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}")
    public ResponseEntity<Object> getManagementWorshopsession(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long academyRoomId,
                                                              @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object obj = academyRoomService.getManagementAcademyRoomWorkShopSession(currentUserObject, academyRoomId,
            workshopSessionId);
        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "workshopSessionId", HttpStatus.BAD_REQUEST);
        }
        return ResponseWrapper.response(obj);

    }

    @Authorize
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions")
    public ResponseEntity<Object> getManagementAcademyRoomWorkShopSessions(HttpServletRequest httpServletRequest,
                                                                           @PathVariable Long academyRoomId,
                                                                           @RequestParam(defaultValue = "0") Integer pageNo,
                                                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                                                           @RequestParam(defaultValue = "asc") String sortDir) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        CurrentPermissionObject currentPermissionObject = (CurrentPermissionObject) httpServletRequest
            .getAttribute("currentPermissionObject");

        Object obj = true;

        if (currentPermissionObject.getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) {
            obj = true;
        } else {
            obj = resourcePermissionService.getManagementWorkshopSessionIds(currentUserObject.getUserId(),
                academyRoomId);
        }

        if (obj instanceof Boolean) {

            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

            if (sortDir.equals("desc")) {
                paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
            }
            List<WorkshopSessionManagementDto> list = academyRoomService
                .getManagementAcademyRoomWorkShopSessions(currentUserObject, academyRoomId, null, paging);
            return ResponseWrapper.response(list);

        } else {

            Set<Long> workshopSessionIds = (Set<Long>) obj;

            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

            if (sortDir.equals("desc")) {
                paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
            }
            List<WorkshopSessionManagementDto> list = academyRoomService.getManagementAcademyRoomWorkShopSessions(
                currentUserObject, academyRoomId, workshopSessionIds, paging);
            return ResponseWrapper.response(list);

        }
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.create,
        resource = ResourceUtil.mar, resourceId = "academyRoomId")
    @PostMapping("management/academyRooms/{academyRoomId}/workshopSessions")
    public ResponseEntity<?> createAcademyRoomWorkShopSession(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long academyRoomId,
                                                              @Valid @RequestBody PostAcademyRoomWorkShopSessionDto academyRoomWorkShopSessionRequest,
                                                              BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.createManagementAcademyRoomWorkShopSession(currentUserObject,
            academyRoomWorkShopSessionRequest, academyRoomId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @DeleteMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}")
    public ResponseEntity<Object> deleteAcademyRoomWorkShopSession(HttpServletRequest httpServletRequest,
                                                                   @PathVariable Long academyRoomId,
                                                                   @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.deleteManagementAcademyRoomWorkShopSession(currentUserObject, academyRoomId,
            workshopSessionId);
        if (data instanceof String) {
            return ResponseWrapper.response400((String) data, "workshopSessionId");
        }
        return ResponseWrapper.response(null, "workshopSession deleted " + academyRoomId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/publish")
    public ResponseEntity<?> publishAcademyRoomRoomWorkShopSession(HttpServletRequest httpServletRequest,
                                                                   @PathVariable Long academyRoomId,
                                                                   @PathVariable Long workshopSessionId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.publishManagementAcademyRoomWorkShopSession(currentUserObject, academyRoomId,
            workshopSessionId);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.edit, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/share")
    public ResponseEntity<?> shareAcademyRoomWorkShopSession(HttpServletRequest httpServletRequest,
                                                             @PathVariable Long academyRoomId,
                                                             @PathVariable Long workshopSessionId,
                                                             @Valid @RequestBody List<PutAcademyRoomShareDto> putAcademyRoomShareDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.shareAcademyRoomWorkShopSession(currentUserObject, academyRoomId, workshopSessionId,
            putAcademyRoomShareDto);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.edit, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/unShare")
    public ResponseEntity<Object> unShareAcademyRoomWorkShopSession(HttpServletRequest httpServletRequest,
                                                                    @PathVariable Long academyRoomId,
                                                                    @PathVariable Long workshopSessionId,
                                                                    @Valid @RequestBody PutAcademyRoomShareDto putAcademyRoomShareDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.unShareAcademyRoomWorkShopSession(currentUserObject, academyRoomId,
            workshopSessionId, putAcademyRoomShareDto);
        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "workshopSessionId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "workshopsession sharing removed");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_COACHES_AND_TRAINERS, RoleName.ROLE_SUPER_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_MEMBER})
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/share/members")
    public ResponseEntity<Object> dropdownWorkshopSessionShareMembers(HttpServletRequest httpServletRequest,
                                                                      @PathVariable Long academyRoomId,
                                                                      @PathVariable Long workshopSessionId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        List<String> roles = new ArrayList<String>();
        roles.add(RoleName.ROLE_COACHES_AND_TRAINERS);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);
        List<Object> list = academyRoomService.dropdownWorkshopSessionShareMembers(currentUserObject, academyRoomId,
            workshopSessionId, roles);
        return ResponseWrapper.response(list);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/submissions")
    public ResponseEntity<Object> getManagementWorkShopSessionsAllSubmissions(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @RequestParam(defaultValue = "0") Integer pageNo,
        @RequestParam(defaultValue = "50") Integer pageSize,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "all") String filterBy,
        @RequestParam(defaultValue = "") String filterKeyword,
        @RequestParam(defaultValue = "submittedOn") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());

        if (sortDir.equals("asc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        }

        return ResponseWrapper.response(academyRoomService.getManagementWorkShopSessionsAllSubmissions(
            currentUserObject, academyRoomId, workshopSessionId, paging, filterBy, filterKeyword));

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/trainingMaterials")
    public ResponseEntity<Object> getManagementWorkShopSessionTrainingMaterials(HttpServletRequest httpServletRequest,
                                                                                @PathVariable Long academyRoomId,
                                                                                @PathVariable Long workshopSessionId,
                                                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                                                @RequestParam(defaultValue = "all") String filterBy,
                                                                                @RequestParam(defaultValue = "") String filterKeyword,
                                                                                @RequestParam(defaultValue = "name") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        List<TrainingMaterialManagementDto> list = academyRoomService.getManagementWorkShopSessionTrainingMaterials(
            currentUserObject, academyRoomId, workshopSessionId, paging, filterBy, filterKeyword);
        return ResponseWrapper.response(list);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PostMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/trainingMaterials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createManagementWorkShopSessionTrainingMaterial(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @Valid @ModelAttribute PostTrainingMaterialDto trainingMaterialRequest,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Map<String, Object> fileCheck = utility.checkFile(trainingMaterialRequest.getTrainingfile());

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.createManagementWorkShopSessionTrainingMaterials(currentUserObject,
            trainingMaterialRequest, academyRoomId, workshopSessionId);
        if (data != null) {
            return ResponseWrapper.response(null, "training materials uploaded");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments")
    public ResponseEntity<Object> getManagementWorkShopSessionAssignments(HttpServletRequest httpServletRequest,
                                                                          @PathVariable Long academyRoomId,
                                                                          @PathVariable Long workshopSessionId,
                                                                          @RequestParam(defaultValue = "0") Integer pageNo,
                                                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                                                          @RequestParam(defaultValue = "asc") String sortDir,
                                                                          @RequestParam(defaultValue = "all") String filterBy,
                                                                          @RequestParam(defaultValue = "all") String searchBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
        }

        Object data = academyRoomService.getManagementWorkShopSessionAssignments(currentUserObject, academyRoomId,
            workshopSessionId, paging, filterBy, searchBy);

        return ResponseWrapper.response(data);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}")
    public ResponseEntity<Object> getManagementWorkShopSessionAssignment(HttpServletRequest httpServletRequest,
                                                                         @PathVariable Long academyRoomId,
                                                                         @PathVariable Long workshopSessionId,
                                                                         @PathVariable Long assignmentId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        AssignmentManagementDto assignment = academyRoomService.getManagementWorkShopSessionAssignment(
            currentUserObject, academyRoomId, workshopSessionId, assignmentId);
        return ResponseWrapper.response(assignment);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/submissions")
    public ResponseEntity<Object> getManagementWorkShopSessionAssignmentSubmissions(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @PathVariable Long assignmentId,
        @RequestParam(defaultValue = "0") Integer pageNo,
        @RequestParam(defaultValue = "100") Integer pageSize,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "all") String filterBy,
        @RequestParam(defaultValue = "") String filterKeyword,
        @RequestParam(defaultValue = "name") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        List<AssignmentManagementDto> list = academyRoomService.getWorkShopSessionAssignmentsSubmitted(
            currentUserObject, academyRoomId, workshopSessionId, assignmentId, paging, filterBy, filterKeyword);
        return ResponseWrapper.response(list);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/review")
    public ResponseEntity<Object> reviewManagementWorkShopSessionAssignments(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @PathVariable Long assignmentId,
        @Valid @RequestBody PutAssignmentManagementDto assignmentRequest,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.reviewManagementWorkShopSessionAssignment(currentUserObject, assignmentRequest,
            academyRoomId, workshopSessionId, assignmentId);
        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "assignmentId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "assignments updated");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PostMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createManagementWorkShopSessionAssignments(HttpServletRequest httpServletRequest,
                                                                        @PathVariable Long academyRoomId,
                                                                        @PathVariable Long workshopSessionId,
                                                                        @Valid PostAssignmentManagementDto assignmentRequest,
                                                                        @RequestParam("files") MultipartFile[] files,
                                                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Map<String, Object> fileCheck = utility.checkFile(files);

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return academyRoomService.createManagementWorkShopSessionAssignment(currentUserObject, assignmentRequest,
            academyRoomId, workshopSessionId, files);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/submitted")
    public ResponseEntity<Object> getManagementWorkShopSessionSubmittedAssignment(HttpServletRequest httpServletRequest,
                                                                                  @PathVariable Long academyRoomId,
                                                                                  @PathVariable Long workshopSessionId,
                                                                                  @PathVariable Long assignmentId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.getManagementWorkShopSessionSubmittedAssignment(currentUserObject,
            academyRoomId, workshopSessionId, assignmentId);
        return ResponseWrapper.response(data);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/publish")
    public ResponseEntity<Object> publishManagementWorkShopSessionAssignment(HttpServletRequest httpServletRequest,
                                                                             @PathVariable Long academyRoomId,
                                                                             @PathVariable Long workshopSessionId,
                                                                             @PathVariable Long assignmentId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.publishManagementWorkShopSessionAssignment(currentUserObject, academyRoomId,
            workshopSessionId, assignmentId);
        if (data != null) {
            return ResponseWrapper.response(null, "assignment published");
        }

        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @DeleteMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/assignments/{assignmentId}")
    public ResponseEntity<Object> deleteManagementWorkShopSessionAssignment(HttpServletRequest httpServletRequest,
                                                                            @PathVariable Long academyRoomId,
                                                                            @PathVariable Long workshopSessionId,
                                                                            @PathVariable Long assignmentId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.deleteManagementWorkShopSessionAssignment(currentUserObject, academyRoomId,
            workshopSessionId, assignmentId);
        if (data instanceof String) {
            return ResponseWrapper.response400((String) data, "assignmentId");
        }
        return ResponseWrapper.response(null, "assignment deleted " + assignmentId);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks")
    public ResponseEntity<Object> getManagementWorkShopSessionFeedbackForms(HttpServletRequest httpServletRequest,
                                                                            @PathVariable Long academyRoomId,
                                                                            @PathVariable Long workshopSessionId,
                                                                            @RequestParam(defaultValue = "0") Integer pageNo,
                                                                            @RequestParam(defaultValue = "50") Integer pageSize,
                                                                            @RequestParam(defaultValue = "asc") String sortDir,
                                                                            @RequestParam(defaultValue = "all") String filterBy,
                                                                            @RequestParam(defaultValue = "all") String searchBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
        }

        Object data = academyRoomService.getManagementWorkShopSessionFeedbackForms(currentUserObject, academyRoomId,
            workshopSessionId, paging, filterBy, searchBy);

        return ResponseWrapper.response(data);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PostMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks")
    public ResponseEntity<Object> createManagementWorkShopSessionFeedbackFormTemplates(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @Valid @RequestBody PostFeedbackFormTemplateManagementDto feedbackTemplateRequest,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.createManagementWorkShopSessionFeedbackFormTemplates(currentUserObject,
            feedbackTemplateRequest, workshopSessionId, academyRoomId);

        if (data != null) {
            return ResponseWrapper.response(null, "feedbackFormTemplate created");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}/publish")
    public ResponseEntity<Object> publishManagementWorkShopSessionFeedbackFormTemplates(HttpServletRequest httpServletRequest,
                                                                                        @PathVariable Long academyRoomId,
                                                                                        @PathVariable Long workshopSessionId,
                                                                                        @PathVariable Long feedbackId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.publishManagementWorkShopSessionFeedbackFormTemplates(currentUserObject,
            workshopSessionId, academyRoomId, feedbackId);

        if (data != null) {
            return ResponseWrapper.response(null, "feedbackFormTemplate published");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @DeleteMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}")
    public ResponseEntity<Object> deleteManagementWorkShopSessionFeedbackFormTemplates(HttpServletRequest httpServletRequest,
                                                                                       @PathVariable Long academyRoomId,
                                                                                       @PathVariable Long workshopSessionId,
                                                                                       @PathVariable Long feedbackId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.deleteManagementWorkShopSessionFeedbackFormTemplates(currentUserObject,
            workshopSessionId, academyRoomId, feedbackId);

        if (data != null) {
            return ResponseWrapper.response(null, "feedbackFormTemplate deleted");
        }
        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}/submissions")
    public ResponseEntity<Object> getManagementWorkShopSessionFeedbackSubmissions(HttpServletRequest httpServletRequest,
                                                                                  @PathVariable Long academyRoomId,
                                                                                  @PathVariable Long workshopSessionId,
                                                                                  @PathVariable Long feedbackId,
                                                                                  @RequestParam(defaultValue = "0") Integer pageNo,
                                                                                  @RequestParam(defaultValue = "100") Integer pageSize,
                                                                                  @RequestParam(defaultValue = "asc") String sortDir,
                                                                                  @RequestParam(defaultValue = "all") String filterBy,
                                                                                  @RequestParam(defaultValue = "") String filterKeyword,
                                                                                  @RequestParam(defaultValue = "name") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
        }
        String sortData = sortBy.concat(sortDir);
        List<StartupFeedbackFormsDto> list = academyRoomService.getWorkShopSessionFeedbacksSubmitted(currentUserObject,
            academyRoomId, workshopSessionId, feedbackId, paging, filterBy, filterKeyword, sortData);
        return ResponseWrapper.response(list);
    }

    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}/submissions/coaches/{coachId}")
    public ResponseEntity<Object> getManagementWorkShopSessionFeedbackSubmissionsByCoach(
        HttpServletRequest httpServletRequest,
        @PathVariable Long academyRoomId,
        @PathVariable Long workshopSessionId,
        @PathVariable Long feedbackId,
        @PathVariable Long coachId,
        @RequestParam(defaultValue = "0") Integer pageNo,
        @RequestParam(defaultValue = "100") Integer pageSize,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "all") String filterBy,
        @RequestParam(defaultValue = "") String filterKeyword,
        @RequestParam(defaultValue = "name") String sortBy) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        String sortData = sortBy.concat(sortDir);

        List<StartupFeedbackFormsDto> list = academyRoomService.getManagementWorkShopSessionFeedbackSubmissionsByCoach(
            currentUserObject, academyRoomId, workshopSessionId, feedbackId, coachId, paging, filterBy,
            filterKeyword, sortData);
        return ResponseWrapper.response(list);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/feedbacks/{feedbackId}")
    public ResponseEntity<Object> getManagementWorkShopSessionFeedbackForm(HttpServletRequest httpServletRequest,
                                                                           @PathVariable Long academyRoomId,
                                                                           @PathVariable Long workshopSessionId,
                                                                           @PathVariable Long feedbackId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        FeedbackFormManagementDto feedback = academyRoomService.getManagementWorkShopSessionFeedback(currentUserObject,
            academyRoomId, workshopSessionId, feedbackId);
        return ResponseWrapper.response(feedback);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys")
    public ResponseEntity<Object> getManagementWorkShopSessionSurvey(HttpServletRequest httpServletRequest,
                                                                     @PathVariable Long academyRoomId,
                                                                     @PathVariable Long workshopSessionId,
                                                                     @RequestParam(defaultValue = "0") Integer pageNo,
                                                                     @RequestParam(defaultValue = "50") Integer pageSize,
                                                                     @RequestParam(defaultValue = "asc") String sortDir,
                                                                     @RequestParam(defaultValue = "all") String filterBy,
                                                                     @RequestParam(defaultValue = "all") String searchBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("name").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("name").descending());
        }

        Object data = academyRoomService.getManagementWorkShopSessionSurveys(currentUserObject, academyRoomId,
            workshopSessionId, paging, filterBy, searchBy);

        if (data != null) {
            return ResponseWrapper.response(data);
        }

        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PostMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys")
    public ResponseEntity<Object> createManagementWorkShopSurveyTemplates(HttpServletRequest httpServletRequest,
                                                                          @PathVariable Long academyRoomId,
                                                                          @PathVariable Long workshopSessionId,
                                                                          @Valid @RequestBody PostSurveyTemplateManagementDto surveyTemplateRequest,
                                                                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.createManagementWorkShopSessionSurveyTemplates(currentUserObject,
            surveyTemplateRequest, workshopSessionId, academyRoomId);

        if (data != null) {
            return ResponseWrapper.response(null, "SurveyTemplate created");
        }

        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN,
        RoleName.ROLE_MANAGEMENT_TEAM_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws, resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}/publish")
    public ResponseEntity<Object> publishManagementWorkShopSessionSurveyTemplates(HttpServletRequest httpServletRequest,
                                                                                  @PathVariable Long academyRoomId,
                                                                                  @PathVariable Long workshopSessionId,
                                                                                  @PathVariable Long surveyId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.publishManagementWorkShopSessionSurveyTemplates(currentUserObject,
            workshopSessionId, academyRoomId, surveyId);

        if (data != null) {
            return ResponseWrapper.response(null, "SurveyTemplate published");
        }

        return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @DeleteMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}")
    public ResponseEntity<Object> deleteManagementWorkShopSessionSurveyTemplates(HttpServletRequest httpServletRequest,
                                                                                 @PathVariable Long academyRoomId,
                                                                                 @PathVariable Long workshopSessionId,
                                                                                 @PathVariable Long surveyId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.deleteManagementWorkShopSessionSurveyTemplates(currentUserObject,
            workshopSessionId, academyRoomId, surveyId);

        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response400((String) data, "surveyId");
            }
            return ResponseWrapper.response(null, "SurveyTemplate deleted");
        }

        return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}/submissions")
    public ResponseEntity<Object> getManagementWorkShopSessionSurveySubmissions(HttpServletRequest httpServletRequest,
                                                                                @PathVariable Long academyRoomId,
                                                                                @PathVariable Long workshopSessionId,
                                                                                @PathVariable Long surveyId,
                                                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                                                @RequestParam(defaultValue = "100") Integer pageSize,
                                                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                                                @RequestParam(defaultValue = "all") String filterBy,
                                                                                @RequestParam(defaultValue = "") String filterKeyword,
                                                                                @RequestParam(defaultValue = "name") String sortBy) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        List<SurveyManagementDto> list = academyRoomService.getWorkShopSessionSurveysSubmitted(currentUserObject,
            academyRoomId, workshopSessionId, surveyId, paging, filterBy, filterKeyword);
        return ResponseWrapper.response(list);

    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws, resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}")
    public ResponseEntity<Object> getManagementWorkShopSessionSurvey(HttpServletRequest httpServletRequest,
                                                                     @PathVariable Long academyRoomId,
                                                                     @PathVariable Long workshopSessionId,
                                                                     @PathVariable Long surveyId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        SurveyManagementDto survey = academyRoomService.getManagementWorkShopSessionSurvey(currentUserObject,
            academyRoomId, workshopSessionId, surveyId);
        return ResponseWrapper.response(survey);

    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws, resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}/submitted")
    public ResponseEntity<Object> getManagementWorkShopSessionSubmittedSurvey(HttpServletRequest httpServletRequest,
                                                                              @PathVariable Long academyRoomId,
                                                                              @PathVariable Long workshopSessionId,
                                                                              @PathVariable Long surveyId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Object data = academyRoomService.getManagementWorkShopSessionSubmittedSurvey(currentUserObject, academyRoomId,
            workshopSessionId, surveyId);
        return ResponseWrapper.response(data);

    }

    @Authorize(roles = {
        RoleName.ROLE_SUPER_ADMIN}, permission = PermissionUtil.get, resource = ResourceUtil.mws,
        resourceId = "workshopSessionId", parentId = "academyRoomId", parentResource = ResourceUtil.mar)
    @PutMapping(value = "management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/surveys/{surveyId}/review")
    public ResponseEntity<Object> reviewManagementWorkShopSessionSurvey(HttpServletRequest httpServletRequest,
                                                                        @PathVariable Long academyRoomId,
                                                                        @PathVariable Long workshopSessionId,
                                                                        @PathVariable Long surveyId,
                                                                        @Valid @RequestBody PutSurveyManagementDto surveyRequest,
                                                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object data = academyRoomService.reviewManagementWorkShopSessionSurvey(currentUserObject, surveyRequest,
            academyRoomId, workshopSessionId, surveyId);
        if (data != null) {
            if (data instanceof String) {
                return ResponseWrapper.response((String) data, "surveyId", HttpStatus.BAD_REQUEST);
            }
            return ResponseWrapper.response(null, "survey updated");
        }
        return ResponseWrapper.response("unknow error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @GetMapping("management/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}/coaches")
    public ResponseEntity<?> listFeedbackCoaches(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long academyRoomId,
                                                 @PathVariable Long workshopSessionId,
                                                 @RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "1000") Integer pageSize,
                                                 @RequestParam(defaultValue = "asc") String sortDir,
                                                 @RequestParam(defaultValue = "email") String sortBy,
                                                 @RequestParam(defaultValue = "email") String filterBy,
                                                 @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return academyRoomService.listFeedbackCoaches(currentUserObject, academyRoomId, workshopSessionId, paging);

    }

}
