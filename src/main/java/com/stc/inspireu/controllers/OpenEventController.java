package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostOpenEventDto;
import com.stc.inspireu.services.OpenEventService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/${api.version}/management/openEvents")
@RequiredArgsConstructor
public class OpenEventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OpenEventService openEventService;

    @PostMapping("")
    public ResponseEntity<?> createPublicCalendarEvent(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestBody PostOpenEventDto postOpenEventDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return openEventService.createPublicCalendarEvent(currentUserObject, postOpenEventDto);

    }

    @PutMapping("{openEventId}")
    public ResponseEntity<?> updatePublicCalendarEvent(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long openEventId,
                                                       @Valid @RequestBody PostOpenEventDto postOpenEventDto,
                                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return openEventService.updatePublicCalendarEvent(currentUserObject, openEventId, postOpenEventDto);

    }

    @GetMapping("{openEventId}")
    public ResponseEntity<?> getPublicCalendarEvent(HttpServletRequest httpServletRequest,
                                                    @PathVariable Long openEventId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return openEventService.getPublicCalendarEvent(currentUserObject, openEventId);

    }

    @GetMapping("intakePrograms")
    public ResponseEntity<?> getAllIntakePrograms(@RequestParam(defaultValue = "0") Integer pageNo,
                                                  HttpServletRequest httpServletRequest,
                                                  @RequestParam(defaultValue = "500") Integer pageSize,
                                                  @RequestParam(defaultValue = "desc") String sortDir,
                                                  @RequestParam(defaultValue = "createdOn") String sortBy) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return openEventService.getAllIntakePrograms(currentUserObject, paging);
    }

    @GetMapping("intakeProgram/{intakeProgramId}/{year}/{month}/{day}")
    public ResponseEntity<?> getCalendarUpComingEvents(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long intakeProgramId,
                                                       @PathVariable Integer day,
                                                       @PathVariable Integer month,
                                                       @PathVariable Integer year,
                                                       @RequestParam(value = "timezone", defaultValue = "UTC") String timezone) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return openEventService.getUpComingEvents(currentUserObject, intakeProgramId, timezone, month, year, day);
    }

    @DeleteMapping("{openEventId}")
    public ResponseEntity<?> getCalendarUpComingEvents(HttpServletRequest httpServletRequest,
                                                       @PathVariable Long openEventId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return openEventService.deleteEventById(currentUserObject, openEventId);
    }

    @GetMapping("{openEventId}/intakeProgram/{intakeProgramId}/copyLink")
    public ResponseEntity<?> getEventLink(HttpServletRequest httpServletRequest,
                                          @PathVariable Long openEventId,
                                          @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        return openEventService.getEventLink(currentUserObject, openEventId, intakeProgramId);
    }

}
