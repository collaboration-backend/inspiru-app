package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.BeneficiaryApplicationsDTO;
import com.stc.inspireu.dtos.BeneficiarySignupDTO;
import com.stc.inspireu.dtos.JsonResponseDTO;
import com.stc.inspireu.services.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}/beneficiary")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping(value = "signup")
    public ResponseEntity<JsonResponseDTO<Long>> signup(@RequestBody @Valid BeneficiarySignupDTO signupDTO) {
        Long id = beneficiaryService.signup(signupDTO);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Registration is successful", id));
    }

    @PutMapping(value = "otp/{userId}")
    public ResponseEntity<JsonResponseDTO<Void>> resendOtp(@PathVariable Long userId) {
        beneficiaryService.sendOTP(userId);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "OTP successfully sent"));
    }

    @PutMapping(value = "otp/{userId}/{otp}")
    public ResponseEntity<JsonResponseDTO<Void>> verifyOTP(@PathVariable Long userId, @PathVariable Integer otp) {
        beneficiaryService.verifyOTP(userId, otp);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "OTP successfully verified"));
    }

    @GetMapping(value = "applications")
    public ResponseEntity<JsonResponseDTO<Page<BeneficiaryApplicationsDTO>>> applications(
        HttpServletRequest httpServletRequest,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return ResponseEntity.ok(new JsonResponseDTO<>(true,
            beneficiaryService.applications(currentUserObject.getUserId(), PageRequest.of(page, size))));
    }
}
