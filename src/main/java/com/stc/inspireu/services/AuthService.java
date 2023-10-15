package com.stc.inspireu.services;

import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.http.ResponseEntity;

import com.stc.inspireu.dtos.CreateOtpDto;
import com.stc.inspireu.dtos.GetAuthToken;
import com.stc.inspireu.dtos.ResendOtpDto;
import com.stc.inspireu.models.User;

public interface AuthService {

	Object getAuthToken(String email, String password);

	User sendForgotPasswordEmail(String email) throws MessagingException;

	Map<String, Object> resetPassword(String password, String resetPasswordToken);

	boolean verifyAuthTokenDto(String authToken);

	String getAuthTokenSSO(String nameId);

	ResponseEntity<?> getToken(GetAuthToken getAuthToken);

	ResponseEntity<?> createOtp(CreateOtpDto createOtpDto);

	ResponseEntity<?> resendOtp(ResendOtpDto resendOtpDto);

}
