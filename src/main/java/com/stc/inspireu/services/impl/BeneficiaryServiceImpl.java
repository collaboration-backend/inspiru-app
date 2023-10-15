package com.stc.inspireu.services.impl;

import com.stc.inspireu.dtos.BeneficiaryApplicationsDTO;
import com.stc.inspireu.dtos.BeneficiarySignupDTO;
import com.stc.inspireu.dtos.CreateOtpDto;
import com.stc.inspireu.dtos.ResendOtpDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.models.IntakeProgramSubmission;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.IntakeProgramSubmissionRepository;
import com.stc.inspireu.repositories.RoleRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.AuthService;
import com.stc.inspireu.services.BeneficiaryService;
import com.stc.inspireu.utils.PasswordUtil;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordUtil passwordUtil;
    private final RoleRepository roleRepository;
    private final IntakeProgramSubmissionRepository programSubmissionRepository;

    @Override
    public Long signup(BeneficiarySignupDTO signupDTO) {
        if (Objects.nonNull(userRepository.findByEmail(signupDTO.getEmail())))
            throw new CustomRunTimeException("Email already exists");
        User user = new User();
        user.setEmail(signupDTO.getEmail());
        user.setAlias(signupDTO.getName());
        String[] mobileSplit = signupDTO.getMobile().split("-");
        if (mobileSplit.length != 2)
            throw new CustomRunTimeException("Invalid mobile number");
        user.setPhoneDialCode(mobileSplit[0]);
        user.setPhoneCountryCodeIso2(signupDTO.getPhoneCountryCodeIso2());
        user.setPhoneNumber(mobileSplit[1]);
        user.setWillManagement(false);
        user.setOtpVerified(false);
        user.setInvitationStatus(Constant.REGISTERED.toString());
        user.setPassword(passwordUtil.getHashedPassword(signupDTO.getPassword()));
        user.setRole(roleRepository.findByRoleName(RoleName.ROLE_STARTUPS_BENEFICIARY));
        CreateOtpDto createOtpDto = new CreateOtpDto();
        createOtpDto.setEmail(signupDTO.getEmail());
        createOtpDto.setPassword(signupDTO.getPassword());
        createOtpDto.setIsEmail(true);
        user = userRepository.save(user);
        authService.createOtp(createOtpDto);
        return user.getId();
    }

    @Override
    public void sendOTP(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        ResendOtpDto resendOtpDto = new ResendOtpDto();
        resendOtpDto.setIsEmail(true);
        resendOtpDto.setEmail(user.getEmail());
        authService.resendOtp(resendOtpDto);
    }

    @Override
    public void verifyOTP(Long userId, Integer otp) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getOtpVerified().equals(true))
            throw new CustomRunTimeException("Account already verified");
        if (user.getOtp().split("_")[0].equals(otp.toString())) {
            user.setOtpVerified(true);
            this.userRepository.save(user);
        } else
            throw new CustomRunTimeException("Invalid OTP");
    }

    @Override
    public Page<BeneficiaryApplicationsDTO> applications(Long beneficiaryId, Pageable pageable) {
        User user = userRepository.findById(beneficiaryId).orElseThrow(() -> new CustomRunTimeException("Beneficiary not found"));
        Page<IntakeProgramSubmission> applications = programSubmissionRepository.findAllByEmailOrBeneficiaryIdOrderByIdDesc(user.getEmail(), user.getId(), pageable);
        return new PageImpl<>(applications
            .stream()
            .map(this::createDTO)
            .collect(Collectors.toList()), pageable, applications.getTotalElements());
    }

    private BeneficiaryApplicationsDTO createDTO(IntakeProgramSubmission intakeProgramSubmission) {
        BeneficiaryApplicationsDTO dto = new BeneficiaryApplicationsDTO();
        dto.setId(intakeProgramSubmission.getId());
        dto.setName(intakeProgramSubmission.getStartupName());
        dto.setEmail(intakeProgramSubmission.getEmail());
        dto.setStatus(intakeProgramSubmission.getPhase());
        dto.setIntake("Intake " + intakeProgramSubmission.getIntakeProgram().getId());
        dto.setSubmittedDate(new PrettyTime().format(new Date(ZonedDateTime.of(intakeProgramSubmission.getCreatedOn(), ZoneId.systemDefault()).toInstant().toEpochMilli())));
        return dto;
    }
}
