package com.stc.inspireu.controllers;

import com.stc.inspireu.dtos.*;
import com.stc.inspireu.services.ContactUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/${api.version}/contact-us")
public class ContactUsController {

    private final ContactUsService contactUsService;

    @GetMapping("settings")
    public ResponseEntity<JsonResponseDTO<List<ContactUsSubjectDTO>>> findAllSubjectSettings() {
        return ResponseEntity.ok(new JsonResponseDTO<>(true, contactUsService.findAll()));
    }

    @PostMapping("settings")
    public ResponseEntity<JsonResponseDTO<Void>> saveSettings(@RequestBody @Valid List<ContactUsSubjectDTO> contactUsSubjectDTOS) {
        contactUsService.save(contactUsSubjectDTOS);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Successfully saved"));
    }

    @GetMapping("subjects")
    public ResponseEntity<JsonResponseDTO<List<ContactUsSubjectPublicDTO>>> findAllSubjects() {
        return ResponseEntity.ok(new JsonResponseDTO<>(true, contactUsService.findSubjects()));
    }

    @PostMapping("submit")
    public ResponseEntity<JsonResponseDTO<Void>> submit(@RequestBody @Valid ContactUsSubmissionDTO submissionDTO)
        throws MessagingException {
        contactUsService.submit(submissionDTO);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Successfully submitted"));
    }

    @GetMapping("submissions")
    public ResponseEntity<JsonResponseDTO<Page<ContactUsSubmissionResultDTO>>> submissions(
        @RequestParam(required = false) String subject,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new JsonResponseDTO<>(true, contactUsService.findAllSubmissions(subject, page, size)));
    }
}
