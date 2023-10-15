package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.mappers.TrainingMaterialMapper;
import com.stc.inspireu.models.TrainingMaterial;
import com.stc.inspireu.services.AcademyRoomService;
import com.stc.inspireu.utils.ConstantUtility;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${api.version}")
@RequiredArgsConstructor
public class StartupAcademyRoomController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Utility utility;
    private final AcademyRoomService academyRoomService;

    @Autowired
    private TrainingMaterialMapper trainingMaterialMapper;

    @GetMapping("startups/academyRoom/status")
    public ResponseEntity<Object> getAcademyRoomStatus() {
        return ResponseWrapper.response(ConstantUtility.getAcademyRoomStatusList());
    }

    @GetMapping("startups/academyRooms/{academyRoomId}")
    public ResponseEntity<Object> getStartupAcademyRoomsByAcademyRoomById(HttpServletRequest httpServletRequest,
                                                                          @PathVariable Long academyRoomId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Map<String, Object> obj = academyRoomService.getStartupAcademyRoomsById(currentUserObject, academyRoomId);
        return ResponseWrapper.response(obj);
    }

    @PutMapping("startups/academyRooms/{academyRoomId}")
    public ResponseEntity<Object> putStartupAcademyRoom(HttpServletRequest httpServletRequest, @PathVariable Long academyRoomId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Map<String, Object> obj = academyRoomService.putStartupAcademyRoom(currentUserObject, academyRoomId);
        return ResponseWrapper.response(obj);
    }

    @GetMapping("startups/academyRooms/status/{academyRoomStatus}")
    public ResponseEntity<Object> getStartupAcademyRoomsByAcademyRoomStatusNew(HttpServletRequest httpServletRequest,
                                                                               @PathVariable String academyRoomStatus) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<AcademyRoomDto> list = academyRoomService.getStartupAcademyRoomsByAcademyRoomStatus(currentUserObject,
            academyRoomStatus);
        return ResponseWrapper.response(list);
    }

    @GetMapping("startups/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}")
    public ResponseEntity<Object> getStartupAcademyRoomWorksopSession(HttpServletRequest httpServletRequest,
                                                                      @PathVariable Long academyRoomId,
                                                                      @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Object obj = academyRoomService.getStartupAcademyRoomWorksopSession(currentUserObject, academyRoomId,
            workshopSessionId);
        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "academyRoomId", HttpStatus.BAD_REQUEST);
        }
        return ResponseWrapper.response(obj);
    }

    @PutMapping("startups/academyRooms/{academyRoomId}/workshopSessions/{workshopSessionId}")
    public ResponseEntity<Object> putStartupAcademyRoomWorksopSession(HttpServletRequest httpServletRequest,
                                                                      @PathVariable Long academyRoomId,
                                                                      @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Object obj = academyRoomService.putStartupAcademyRoomWorksopSession(currentUserObject, academyRoomId,
            workshopSessionId);
        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "academyRoomId|workshopSessionId", HttpStatus.BAD_REQUEST);
        }
        return ResponseWrapper.response(obj);
    }

    @GetMapping("startups/workshopSessions/{workshopSessionId}/trainingMaterials")
    public ResponseEntity<Object> getWorksopSessionTrainingMaterials(HttpServletRequest httpServletRequest,
                                                                     @PathVariable Long workshopSessionId, @RequestParam(defaultValue = "0") Integer pageNo,
                                                                     @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "asc") String sortDir,
                                                                     @RequestParam(defaultValue = "all") String filterBy, @RequestParam(defaultValue = "") String filterKeyword,
                                                                     @RequestParam(defaultValue = "name") String sortBy) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        Page<TrainingMaterial> list = academyRoomService.getWorksopSessionTrainingMaterials(currentUserObject,
            workshopSessionId, filterKeyword, paging, filterBy);

        Page<GetTrainingMaterialDto> ls = list.map(trainingMaterialMapper::toGetTrainingMaterialDto);

        return ResponseWrapper.response(ls);
    }

    @GetMapping("startups/workshopSessions/{workshopSessionId}/surveys")
    public ResponseEntity<Object> getWorksopSessionSurveys(HttpServletRequest httpServletRequest,
                                                           @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<SurveyDto> list = academyRoomService.getWorksopSessionSurveys(currentUserObject, workshopSessionId);

        return ResponseWrapper.response(list);
    }

    @PostMapping("startups/workshopSessions/{workshopSessionId}/surveys/{surveyId}/submit")
    public ResponseEntity<Object> submitWorksopSessionSurvey(HttpServletRequest httpServletRequest,
                                                             @PathVariable Long workshopSessionId,
                                                             @PathVariable Long surveyId,
                                                             @Valid @RequestBody SubmitStartupSurveyDto submitStartupSurveyDto,
                                                             BindingResult bindingResult) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            academyRoomService.submitWorksopSessionSurvey(currentUserObject, workshopSessionId, surveyId,
                submitStartupSurveyDto);
            return ResponseWrapper.response(null);
        }
    }

    @GetMapping("startups/workshopSessions/{workshopSessionId}/feedbacks")
    public ResponseEntity<Object> getWorksopSessionFeedbacks(HttpServletRequest httpServletRequest,
                                                             @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<GetFeedbackDto> list = academyRoomService.getWorksopSessionFeedbacks(currentUserObject, workshopSessionId);

        return ResponseWrapper.response(list);
    }

    @GetMapping("startups/workshopSessions/{workshopSessionId}/assignments")
    public ResponseEntity<Object> getWorksopSessionAssignments(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long workshopSessionId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        List<GetAssignmentDto> ls = academyRoomService.getWorksopSessionAssignments(currentUserObject,
            workshopSessionId);

        return ResponseWrapper.response(ls);
    }

    @GetMapping("startups/workshopSessions/{workshopSessionId}/assignments/{assignmentId}")
    public ResponseEntity<Object> getWorksopSessionAssignment(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long workshopSessionId,
                                                              @PathVariable Long assignmentId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> obj = academyRoomService.getWorksopSessionAssignment(currentUserObject, workshopSessionId,
            assignmentId);

        return ResponseWrapper.response(obj);
    }

    @PostMapping("startups/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/clone")
    public ResponseEntity<Object> updateWorksopSessionAssignment(HttpServletRequest httpServletRequest,
                                                                 @PathVariable Long workshopSessionId,
                                                                 @PathVariable Long assignmentId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = academyRoomService.cloneWorksopSessionAssignment(currentUserObject, workshopSessionId,
            assignmentId);

        if (data != null) {
            return ResponseWrapper.response(data);
        } else {
            return ResponseWrapper.response400("invalid assignmentId", "assignmentId");
        }
    }

    @PutMapping(value = "startups/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> putWorksopSessionAssignments(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long workshopSessionId,
                                                               @PathVariable Long assignmentId,
                                                               @RequestParam("documents") MultipartFile[] documents) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Map<String, Object> fileCheck = utility.checkFile(documents);

        if (!(boolean) fileCheck.get("isAllow")) {
            return ResponseWrapper.response400((String) fileCheck.get("error"), "files");
        }

        if (documents.length == 0) {
            return ResponseWrapper.response("required atleast one file", "documents", HttpStatus.BAD_REQUEST);
        } else if (documents.length > 5) {
            return ResponseWrapper.response("at a time 5 file maximum", "documents", HttpStatus.BAD_REQUEST);
        } else {
            Object data = academyRoomService.putWorksopSessionAssignments(currentUserObject, workshopSessionId,
                assignmentId, documents);
            if (data != null) {
                return ResponseWrapper.response(null, "files uploaded");
            } else {
                return ResponseWrapper.response("unknown error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @DeleteMapping(value = "startups/workshopSessions/{workshopSessionId}/assignments/{assignmentId}/documents/{documentId}")
    public ResponseEntity<?> deleteWorksopSessionAssignment(HttpServletRequest httpServletRequest,
                                                            @PathVariable Long workshopSessionId,
                                                            @PathVariable Long assignmentId,
                                                            @PathVariable Long documentId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return academyRoomService.deleteWorksopSessionAssignment(currentUserObject, workshopSessionId, assignmentId,
            documentId);
    }

}
