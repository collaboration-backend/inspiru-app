package com.stc.inspireu.controllers;

import com.stc.inspireu.dtos.*;
import com.stc.inspireu.models.User;
import com.stc.inspireu.services.AuthService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AuthService authService;

    @PostMapping("otp")
    public ResponseEntity<?> generateOtp(@Valid @RequestBody CreateOtpDto createOtpDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return authService.createOtp(createOtpDto);
    }

    @PostMapping("resendOtp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpDto resendOtpDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return authService.resendOtp(resendOtpDto);
    }

    @PostMapping("authToken")
    public ResponseEntity<?> getToken(@Valid @RequestBody GetAuthToken getAuthToken, BindingResult bindingResult) {
        LOGGER.debug("getAuthToken");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        return authService.getToken(getAuthToken);
    }

    @PostMapping("token")
    public ResponseEntity<Object> token(@Valid @RequestBody CreateAuthTokenDto createAuthTokenDto,
                                        BindingResult bindingResult) {
        LOGGER.debug("getAuthToken");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Object data = authService.getAuthToken(createAuthTokenDto.getEmail(), createAuthTokenDto.getPassword());

            if (data == null) {
                return ResponseWrapper.response("invalid email or password", "email|password", HttpStatus.UNAUTHORIZED);
            } else {
                return ResponseWrapper.response(data);
            }
        }
    }

    @PostMapping("verifyAuthToken")
    public ResponseEntity<Object> verifyAuthToken(@Valid @RequestBody VerifyAuthTokenDto verifyAuthTokenDto,
                                                  BindingResult bindingResult) throws MessagingException {
        LOGGER.debug("validateAuthToken");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            boolean isValid = authService.verifyAuthTokenDto(verifyAuthTokenDto.getAuthToken());

            if (isValid) {
                return ResponseWrapper.response(null, "authToken is valid");
            } else {
                return ResponseWrapper.response400("invalid authToken", "authToken");
            }

        }

    }

    @PostMapping("forgotPassword")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto,
                                                 BindingResult bindingResult) throws MessagingException {
        LOGGER.debug("forgotPassword");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            User user = authService.sendForgotPasswordEmail(forgotPasswordDto.getEmail());

            if (user != null) {
                return ResponseWrapper.response(null, "reset password link send");
            } else {
                return ResponseWrapper.response400("No user registered with " + forgotPasswordDto.getEmail(), "email");
            }

        }

    }

    @PostMapping("resetPassword")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto,
                                                BindingResult bindingResult) {
        LOGGER.debug("resetPassword");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            if (resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
                Map<String, Object> claims = authService.resetPassword(resetPasswordDto.getPassword(),
                    resetPasswordDto.getResetPasswordToken());

                if (claims != null) {
                    return ResponseWrapper.response(null, "reset password success");
                } else {
                    return ResponseWrapper.response400("invalid resetPasswordToken", "resetPasswordToken");
                }
            } else {
                return ResponseWrapper.response400(
                    resetPasswordDto.getConfirmPassword() + " should be equal to password", "confirmPassword");
            }
        }

    }
}
