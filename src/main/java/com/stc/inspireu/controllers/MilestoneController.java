package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.MileStoneDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.services.MilestoneService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.util.Collections;

@RestController
@RequestMapping("/api/${api.version}/management")
@RequiredArgsConstructor
public class MilestoneController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MilestoneService milestoneService;

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PostMapping("/milestones/publish")
    public ResponseEntity<?> createMileStone(HttpServletRequest httpServletRequest,
                                             @RequestBody MileStoneDto mileStoneDto,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");
            Boolean result = milestoneService.createMileStone(currentUserObject, mileStoneDto,
                Constant.PUBLISHED.toString());

            return ResponseWrapper.response(Boolean.TRUE.equals(result) ? "Milestone saved and published" : "Failed");
        }

    }

    @PostMapping("/milestones/draft")
    public ResponseEntity<?> draftMileStone(HttpServletRequest httpServletRequest,
                                            @RequestBody MileStoneDto mileStoneDto,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");
            Boolean result = milestoneService.createMileStone(currentUserObject, mileStoneDto,
                Constant.PUBLISHED.toString());

            return ResponseWrapper.response(Boolean.TRUE.equals(result) ? "Milestone drafted" : "Failed");
        }

    }

    @GetMapping("/milestones")
    public ResponseEntity<?> getMilestone(HttpServletRequest httpServletRequest,
                                          @RequestParam(defaultValue = "0") Integer pageNo,
                                          @RequestParam(defaultValue = "50") Integer pageSize) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return ResponseWrapper.response(milestoneService.milestoneList(currentUserObject, pageNo, pageSize));

    }

    @GetMapping("/milestones/{milestoneId}")
    public ResponseEntity<?> getMilestoneById(@PathVariable Long milestoneId) {
        return ResponseWrapper.response(Collections.emptyList());

    }

    @PutMapping("/milestones/{milestoneId}")
    public ResponseEntity<?> updateMilestoneById(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long milestoneID,
                                                 @RequestBody MileStoneDto mileStoneDto) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Boolean result = milestoneService.updateMileStoneById(currentUserObject, mileStoneDto, milestoneID);

        return ResponseWrapper.response(Boolean.TRUE.equals(result) ? "updated" : "failed");

    }

    @DeleteMapping("/milestones/{milestoneId}")
    public ResponseEntity<?> deleteMilestoneById(@PathVariable Long milestoneId) {
        try {
            return ResponseWrapper.response("deleted", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.response("deleting failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/milestones/{milestoneId}/status/{isPublish}")
    public ResponseEntity<?> updateStatus(@PathVariable Long milestoneId, @PathVariable Boolean isPublish) {
        Boolean result = milestoneService.updateStatus(milestoneId,
            Boolean.TRUE.equals(isPublish) ? Constant.PUBLISHED.toString() : Constant.DRAFT.toString());
        return ResponseWrapper.response(Boolean.TRUE.equals(result) ? "status updated" : "status updating failed");

    }

}
