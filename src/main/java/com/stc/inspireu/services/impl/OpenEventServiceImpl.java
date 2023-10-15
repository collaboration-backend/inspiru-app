package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.GetIntakeProgramDto;
import com.stc.inspireu.dtos.PostOpenEventDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.IntakeProgramMapper;
import com.stc.inspireu.mappers.OpenEventMapper;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.OpenEvent;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.IntakeProgramRepository;
import com.stc.inspireu.repositories.OpenEventRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.OpenEventService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenEventServiceImpl implements OpenEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OpenEventRepository openEventRepository;
    private final UserRepository userRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final Utility utility;
    private final JwtUtil jwtUtil;
    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.publicCal}")
    private String publicCal;
    private final OpenEventMapper openEventMapper;
    private final IntakeProgramMapper intakeProgramMapper;

    @Transactional
    @Override
    public ResponseEntity<?> createPublicCalendarEvent(CurrentUserObject currentUserObject,
                                                       PostOpenEventDto postOpenEventDto) {
        User user = userRepository.findById(currentUserObject.getUserId())
            .orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (openEventRepository.existsByIntakeProgram_IdAndEventPhase(
            postOpenEventDto.getIntakeProgramId(), postOpenEventDto.getEventPhase())) {
            return ResponseWrapper.response400("Event Already Created", "intakeProgramId|eventPhase");
        }
        IntakeProgram ip = intakeProgramRepository.findById(postOpenEventDto.getIntakeProgramId())
            .orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        OpenEvent oe = new OpenEvent();
        oe.setCreatedUser(user);
        oe.setDescriptionSection(postOpenEventDto.getDescription());
        oe.setEventName(postOpenEventDto.getEventName());
        oe.setEventPhase(postOpenEventDto.getEventPhase());
        oe.setIntakeProgram(ip);
        oe.setIsActive(postOpenEventDto.getIsActive());
        oe.setJsonEventInfo(postOpenEventDto.getJsonEventInfo());
        oe.setMeetingLink(postOpenEventDto.getMeetingLink());
        oe.setMeetingRoom(postOpenEventDto.getMeetingRoom());
        oe.setSchedulingMethod(postOpenEventDto.getSchedulingMethod());
        oe.setSessionEnd(new Date(postOpenEventDto.getSessionEnd()));
        oe.setSessionStart(new Date(postOpenEventDto.getSessionStart()));
        oe.setTimezone(postOpenEventDto.getTimezone());
        openEventRepository.save(oe);
        return ResponseWrapper.response(null, "Meeting Scheduled");
    }

    @Override
    public ResponseEntity<?> updatePublicCalendarEvent(CurrentUserObject currentUserObject, Long eventId,
                                                       PostOpenEventDto postOpenEventDto) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        OpenEvent oe = openEventRepository.findById(eventId).orElseThrow(() -> new CustomRunTimeException("Event not found"));
        IntakeProgram ip = intakeProgramRepository.findById(postOpenEventDto.getIntakeProgramId()).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        oe.setDescriptionSection(postOpenEventDto.getDescription());
        oe.setEventName(postOpenEventDto.getEventName());
        // oe.setEventPhase(postOpenEventDto.getEventPhase());
        oe.setIntakeProgram(ip);
        oe.setIsActive(postOpenEventDto.getIsActive());
        oe.setJsonEventInfo(postOpenEventDto.getJsonEventInfo());
        oe.setMeetingLink(postOpenEventDto.getMeetingLink());
        oe.setMeetingRoom(postOpenEventDto.getMeetingRoom());
        oe.setSchedulingMethod(postOpenEventDto.getSchedulingMethod());
        oe.setSessionEnd(new Date(postOpenEventDto.getSessionEnd()));
        oe.setSessionStart(new Date(postOpenEventDto.getSessionStart()));
        oe.setTimezone(postOpenEventDto.getTimezone());
        openEventRepository.save(oe);
        return ResponseWrapper.response(null, "Meeting updated");
    }

    @Transactional
    @Override
    public ResponseEntity<?> getAllIntakePrograms(CurrentUserObject currentUserObject, Pageable paging) {
        Page<IntakeProgram> ls = intakeProgramRepository.findByStatusNotIn(
            Arrays.asList(Constant.EXISTING_INTAKE.toString(), Constant.DRAFT.toString()), paging);
        Page<GetIntakeProgramDto> list = ls.map(intakeProgramMapper::toGetIntakeProgramDto);
        return ResponseWrapper.response(list);

    }

    @Transactional
    @Override
    public ResponseEntity<?> getUpComingEvents(CurrentUserObject currentUserObject, Long intakeProgramId,
                                               String timeZone, Integer month, Integer year, Integer day) {
        Date date = utility.convertToDate(year, month, day, timeZone);
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram ip = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        if (date != null) {
//                    List<OpenEvent> openEvents = openEventRepository.findUpComingEvent(intakeProgramId, date);
            Pageable paging = PageRequest.of(0, 10, Sort.by("id").descending());
            List<OpenEvent> openEvents = openEventRepository.findTop10(intakeProgramId, paging);
            return ResponseWrapper.response(openEventMapper.toGetOpenEventDtoList(openEvents));
        }
        return ResponseWrapper.response(Collections.emptyList());
    }

    @Override
    public ResponseEntity<?> getPublicCalendarEvent(CurrentUserObject currentUserObject, Long eventId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return openEventRepository.findById(eventId).map(event -> ResponseWrapper.response(openEventMapper.toGetOpenEventDto(event)))
            .orElseThrow(() -> new CustomRunTimeException("Event not found"));
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteEventById(CurrentUserObject currentUserObject, Long eventId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        OpenEvent openEvent = openEventRepository.findById(eventId).orElseThrow(() -> new CustomRunTimeException("Event not found"));
        openEventRepository.delete(openEvent);
        return ResponseWrapper.response("event deleted", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getEventLink(CurrentUserObject currentUserObject, Long eventId, Long intakeProgramId) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        IntakeProgram ip = intakeProgramRepository.findById(intakeProgramId).orElseThrow(() -> new CustomRunTimeException("Intake not found"));
        openEventRepository.findById(eventId).orElseThrow(() -> new CustomRunTimeException("Event not found"));
        Map<String, Object> data = new HashMap<>();
        data.put("intakeProgramId", ip.getId());
        data.put("intakeProgramName", ip.getProgramName());
        data.put("openEventId", eventId);
        data.put("email", null);
        long milliSeconds = (ip.getPeriodEnd().getTime() - new Date().getTime());
        String claims = jwtUtil.genericJwtToken(data, milliSeconds);
        String link = uiUrl + publicCal + "?formToken=" + claims;
        return ResponseWrapper.response(link);
    }
}
