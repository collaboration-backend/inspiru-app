package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostMarkCardNotificationDto;
import com.stc.inspireu.dtos.PutGenerateMarkcardDto;
import com.stc.inspireu.services.MarkCardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/${api.version}/management/_markCards")
@RequiredArgsConstructor
public class MarkCardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MarkCardService markCardService;

    @GetMapping("")
    public ResponseEntity<?> getMarkCardList(HttpServletRequest httpServletRequest,
                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "500") Integer pageSize,
                                             @RequestParam(defaultValue = "asc") String sortDir,
                                             @RequestParam(defaultValue = "createdOn") String sortBy,
                                             @RequestParam(defaultValue = "INSPIREU") String filterBy,
                                             @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        }

        return markCardService.getMarkCardList(currentUserObject, paging, filterBy);
    }

    @GetMapping("{markCardId}/academyRooms")
    public ResponseEntity<?> getAcademyRoomsList(HttpServletRequest httpServletRequest,
                                                 @RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "500") Integer pageSize,
                                                 @RequestParam(defaultValue = "asc") String sortDir,
                                                 @RequestParam(defaultValue = "createdOn") String sortBy,
                                                 @RequestParam(defaultValue = "INSPIREU") String filterBy,
                                                 @RequestParam(defaultValue = "") String filterKeyword,
                                                 @PathVariable Long markCardId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("createdOn").descending());
        }

        return markCardService.getAcademyRoomsList(currentUserObject, paging, filterBy, markCardId);
    }

    @Authorize
    @GetMapping("{markCardId}/academyRooms/{academyRoomId}/startups")
    public ResponseEntity<?> markCardStartupList(HttpServletRequest httpServletRequest,
                                                 @PathVariable Long markCardId,
                                                 @RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "500") Integer pageSize,
                                                 @RequestParam(defaultValue = "asc") String sortDir,
                                                 @RequestParam(defaultValue = "createdOn") String sortBy,
                                                 @RequestParam(defaultValue = "") String filterBy,
                                                 @RequestParam(defaultValue = "") String filterKeyword,
                                                 @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return markCardService.markCardStartupList(currentUserObject, markCardId, academyRoomId, paging, filterKeyword);
    }

    @Authorize
    @GetMapping("{markCardId}/summary")
    public ResponseEntity<?> getMarkCardSummary(HttpServletRequest httpServletRequest,
                                                @PathVariable Long markCardId,
                                                @RequestParam(defaultValue = "0") Integer pageNo,
                                                @RequestParam(defaultValue = "500") Integer pageSize,
                                                @RequestParam(defaultValue = "asc") String sortDir,
                                                @RequestParam(defaultValue = "createdOn") String sortBy,
                                                @RequestParam(defaultValue = "") String filterBy,
                                                @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return markCardService.marCardSummary(currentUserObject, markCardId, paging, filterKeyword);
    }

    @Authorize
    @GetMapping("{markCardId}/academyRooms/{academyRoomId}/status")
    public ResponseEntity<?> markCardAcademRommStatus(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long markCardId,
                                                      @PathVariable Long academyRoomId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.markCardAcademRommStatus(currentUserObject, markCardId, academyRoomId);
    }

    @Authorize
    @PutMapping("{markCardId}/academyRooms/{academyRoomId}/generate")
    public ResponseEntity<?> generateMarkcard(HttpServletRequest httpServletRequest,
                                              @PathVariable Long markCardId,
                                              @PathVariable Long academyRoomId,
                                              @RequestBody PutGenerateMarkcardDto putGenerateMarkcardDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.generateMarkcard(currentUserObject, markCardId, academyRoomId, putGenerateMarkcardDto);
    }

    @Authorize
    @PutMapping("{markCardId}/academyRooms/{academyRoomId}/saveTemplate")
    public ResponseEntity<?> saveTemplate(HttpServletRequest httpServletRequest,
                                          @PathVariable Long markCardId,
                                          @PathVariable Long academyRoomId,
                                          @RequestBody PutGenerateMarkcardDto putGenerateMarkcardDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.saveTemplate(currentUserObject, markCardId, academyRoomId, putGenerateMarkcardDto);
    }

    @Authorize
    @GetMapping("{markCardId}/academyRooms/{academyRoomId}/startups/{startupId}/info")
    public ResponseEntity<?> getMarkCardAcademRoomStartupInfo(HttpServletRequest httpServletRequest,
                                                              @PathVariable Long markCardId,
                                                              @PathVariable Long academyRoomId,
                                                              @PathVariable Long startupId,
                                                              @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.getMarkCardAcademRoomStartupInfo(currentUserObject, markCardId, academyRoomId, startupId,
            timezone);
    }

    @Authorize
    @PutMapping("{markCardId}/academyRooms/{academyRoomId}/startups/{startupId}/notify")
    public ResponseEntity<?> notifyStartup(HttpServletRequest httpServletRequest,
                                           @PathVariable Long markCardId,
                                           @PathVariable Long academyRoomId,
                                           @PathVariable Long startupId,
                                           @RequestBody PostMarkCardNotificationDto postMarkCardNotificationDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.notifyStartup(currentUserObject, markCardId, academyRoomId, startupId,
            postMarkCardNotificationDto);
    }

    @Authorize
    @PutMapping("{markCardId}/academyRooms/{academyRoomId}/startups/{startupId}/alert")
    public ResponseEntity<?> alertStartup(HttpServletRequest httpServletRequest,
                                          @PathVariable Long markCardId,
                                          @PathVariable Long academyRoomId,
                                          @PathVariable Long startupId,
                                          @RequestBody PostMarkCardNotificationDto postMarkCardNotificationDto) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.alertStartup(currentUserObject, markCardId, academyRoomId, startupId,
            postMarkCardNotificationDto);
    }

    @GetMapping("{markCardId}/summary/columns")
    public ResponseEntity<?> getSummaryColumsList(HttpServletRequest httpServletRequest, @PathVariable Long markCardId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return markCardService.getSummaryColumsList(currentUserObject, markCardId);
    }

    @Authorize
    @GetMapping("{markCardId}/summary/startups")
    public ResponseEntity<?> getSummaryStartups(HttpServletRequest httpServletRequest, @PathVariable Long markCardId,
                                                @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "100") Integer pageSize,
                                                @RequestParam(defaultValue = "asc") String sortDir, @RequestParam(defaultValue = "createdOn") String sortBy,
                                                @RequestParam(defaultValue = "") String filterBy, @RequestParam(defaultValue = "") String filterKeyword) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        return markCardService.getSummaryStartups(currentUserObject, markCardId, filterBy, filterKeyword, paging);
    }

}
