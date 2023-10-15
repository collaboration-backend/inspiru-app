package com.stc.inspireu.services;

import com.stc.inspireu.dtos.BeneficiaryApplicationsDTO;
import com.stc.inspireu.dtos.BeneficiarySignupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BeneficiaryService {

    Long signup(BeneficiarySignupDTO signupDTO);

    void sendOTP(Long userId);

    void verifyOTP(Long userId,Integer otp);

    Page<BeneficiaryApplicationsDTO> applications(Long beneficiaryId, Pageable pageable);
}
