package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.NotificationIdDto;
import com.stc.inspireu.jpa.projections.ProjectNotification;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/${api.version}")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final NotificationService notificationService;

    @GetMapping("startups/notifications")
    public ResponseEntity<Object> getStartupNotifications(HttpServletRequest httpServletRequest,
                                                          @RequestParam(defaultValue = "0") Integer pageNo,
                                                          @RequestParam(defaultValue = "100") Integer pageSize,
                                                          @RequestParam(defaultValue = "all") String filterBy) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        Page<ProjectNotification> list = notificationService.getStartupNotifications(currentUserObject, filterBy,
            paging);
        return ResponseWrapper.response(list);
    }

    @GetMapping("management/notifications")
    public ResponseEntity<Object> getManagementNotifications(HttpServletRequest httpServletRequest,
                                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                                             @RequestParam(defaultValue = "100") Integer pageSize,
                                                             @RequestParam(defaultValue = "all") String filterBy) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        Page<ProjectNotification> list = notificationService.getManagementNotifications(currentUserObject, filterBy,
            paging);
        return ResponseWrapper.response(list);
    }

    @PutMapping("notifications/markAsRead")
    public ResponseEntity<Object> markAsRead(HttpServletRequest httpServletRequest,
                                             @Valid @RequestBody NotificationIdDto notificationIdDto,
                                             BindingResult bindingResult) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            notificationService.markAsRead(currentUserObject, notificationIdDto);
            return ResponseWrapper.response("done");
        }

    }

    @GetMapping("management/notifications/alltime")
    public ResponseEntity<Object> getAlltime(HttpServletRequest httpServletRequest,
                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "100") Integer pageSize,
                                             @RequestParam(defaultValue = "all") String filterBy) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        Page<ProjectNotification> list = notificationService.getAlltime(currentUserObject, filterBy, paging);
        return ResponseWrapper.response(list);
    }

}
