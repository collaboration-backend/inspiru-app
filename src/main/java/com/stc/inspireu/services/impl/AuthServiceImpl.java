package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.CreateOtpDto;
import com.stc.inspireu.dtos.GetAuthToken;
import com.stc.inspireu.dtos.ResendOtpDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.AuthService;
import com.stc.inspireu.services.ChatService;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.utils.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${superadmin.email}")
    private String superadminEmail;

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.resetPasswordPath}")
    private String resetPasswordPath;

    private final ChatService chatService;

    @Value("${security.auth.max-failure-attempts}")
    private int maxFailureAttempts;

    @Value("${security.auth.account-suspend-minutes}")
    private int accountSuspendMinutes;

    @Value("${security.auth.failure-check-minutes}")
    private int failureCheckMinutes;

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final Utility utility;
    private final Environment environment;
    private final SMSUtil sMSUtil;

    @Transactional
    @Override
    public Object getAuthToken(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        } else {
            if (!user.getInvitationStatus().equals(Constant.REGISTERED.toString())) {
                return ResponseWrapper.response400("User either blocked/not registered", "email");
            }
            if (passwordUtil.verifyPassword(password, user.getPassword())) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", user.getId());
                claims.put("email", user.getEmail());
                claims.put("roleId", user.getRole().getId());
                claims.put("roleName", user.getRole().getRoleName());
                String jwt = jwtUtil.generateAuthToken(claims);
                Map<String, Object> data = new HashMap<>();
                data.put("authToken", jwt);
                data.put("userId", user.getId());
                data.put("email", user.getEmail());
                data.put("roleId", user.getRole().getId());
                data.put("roleName", user.getRole().getRoleName());
                data.put("assetToken", jwtUtil.generateAssetJwtToken(null, true));
                return data;
            } else {
                return null;
            }
        }
    }

    @Transactional
    @Override
    public User sendForgotPasswordEmail(String email) throws MessagingException {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            Map<String, Object> claims = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("userId", user.getId());
                    put("email", user.getEmail());
                }
            };
            String token = jwtUtil.genericJwtToken(claims);
            user.setPasswordResetToken(token);
            userRepository.save(user);
            LOGGER.debug("mail sending");
            MailMetadata mailMetadata = new MailMetadata();
            Map<String, Object> props = new HashMap<>();
            String link = uiUrl + resetPasswordPath + "/" + token;
            props.put("resetLink", link);
            props.put("toMail", email);
            mailMetadata.setFrom("");
            mailMetadata.setTo(email);
            mailMetadata.setProps(props);
            mailMetadata.setSubject("Reset password");
            mailMetadata.setTemplateFile("reset-password");
            notificationService.sendForgotPasswordNotification(mailMetadata);
            // emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
        }
        return user;
    }

    @Transactional
    @Override
    public Map<String, Object> resetPassword(String password, String token) {
        Map<String, Object> claims = jwtUtil.getClaimsFromGenericToken(token);
        if (claims != null) {
            User user = userRepository.findByEmail((String) claims.get("email"));
            if (user != null) {
                String hashedPassword = passwordUtil.getHashedPassword(password);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", user.getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(user.getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("Reset password");
                mailMetadata.setTemplateFile("reset-password");
                notificationService.sendResetPasswordNotification(mailMetadata);
                return claims;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public boolean verifyAuthTokenDto(String authToken) {
        Map<String, Object> claims = jwtUtil.getClaimsFromAuthToken(authToken);
        if (claims != null) {
            User u = userRepository.findByEmail((String) claims.get("email"));
            return u != null && u.getInvitationStatus().equals(Constant.REGISTERED.toString());
        } else {
            return false;
        }
    }

    @Transactional
    @Override
    public String getAuthTokenSSO(String nameId) {
        User user = userRepository.findByEmailAndInvitationStatus(nameId, Constant.REGISTERED.toString());
        if (user == null) {
            return "no_user_found";
        } else {
            Map<String, Object> claims = new HashMap<String, Object>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("roleId", user.getRole().getId());
            claims.put("roleName", user.getRole().getRoleName());
            return jwtUtil.generateAuthToken(claims);
        }
    }

    @Override
    public ResponseEntity<?> getToken(GetAuthToken createAuthTokenDto) {
        String email = createAuthTokenDto.getEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseWrapper.response400("user not found", "email");
        } else {
            if (!user.getInvitationStatus().equals(Constant.REGISTERED.toString())) {
                return ResponseWrapper.response400("User either blocked/not registered", "email");
            }
            if (createAuthTokenDto.getIsEmail() != null && createAuthTokenDto.getIsEmail()) {
                String otp = user.getOtp();
                if (otp == null || otp == "") {
                    return ResponseWrapper.response400("incorrect otp", "otp");
                }
                String[] parts = (otp).split("_");
                Integer random = Integer.parseInt(parts[0]);
                String context = parts[1];
                long et = Long.parseLong(parts[2]) + 180000;
                if (!context.equals(createAuthTokenDto.getContext())) {
                    return ResponseWrapper.response400("incorrect otp", "otp");
                }
                // ignore otp on qa
                if (!random.equals(createAuthTokenDto.getOtp())) {
                    return ResponseWrapper.response400("incorrect otp", "otp");
                }
                long now = new Date().toInstant().toEpochMilli();
                if (now > et) {
                    return ResponseWrapper.response400("otp expired", "otp");
                }
            } else {
                try {
                    String res = sMSUtil.validateOtp(createAuthTokenDto.getContext(), createAuthTokenDto.getOtp() + "");
                    if (res.equals("error")) {
                        return ResponseWrapper.response400("otp expired", "otp");
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                    return ResponseWrapper.response400("otp expired", "otp");
                }
            }
            Map<String, Object> claims = new HashMap<String, Object>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("roleId", user.getRole().getId());
            claims.put("roleName", user.getRole().getRoleName());
            String jwt = jwtUtil.generateAuthToken(claims);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("authToken", jwt);
            data.put("userId", user.getId());
            data.put("email", user.getEmail());
            data.put("roleId", user.getRole().getId());
            data.put("unReadMessageCount", chatService.unreadCount(user.getId()));
            data.put("roleName", user.getRole().getRoleName());
            data.put("assetToken", jwtUtil.generateAssetJwtToken(null, true));
            return ResponseWrapper.response(data);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> createOtp(CreateOtpDto createAuthTokenDto) {
        String email = createAuthTokenDto.getEmail();
        String password = createAuthTokenDto.getPassword();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseWrapper.response400("user not found", "email");
        } else {
            if (!user.getInvitationStatus().equals(Constant.REGISTERED.toString())) {
                return ResponseWrapper.response400("User either blocked/not registered", "email");
            }
            LocalDateTime lastFailureTime = user.getLastFailureTime();
            if (Objects.nonNull(lastFailureTime) &&
                lastFailureTime.isAfter(LocalDateTime.now().minusMinutes(accountSuspendMinutes)) && user.getFailureCount() >= maxFailureAttempts)
                throw new CustomRunTimeException("Your account is temporarily blocked due to multiple failed attempts. " +
                    "Please try after " + (LocalDateTime.now().minusMinutes(accountSuspendMinutes).until(lastFailureTime, ChronoUnit.MINUTES) + 1) + " minutes",
                    HttpStatus.BAD_REQUEST);
            if (passwordUtil.verifyPassword(password, user.getPassword())) {
                Map<String, Object> data = new HashMap<String, Object>();
                Integer random = utility.getOtp();
                String context = utility.getAlhpaNumeric(50);
                long createAt = new Date().toInstant().toEpochMilli();
                if (createAuthTokenDto.getIsEmail() != null && createAuthTokenDto.getIsEmail()) {
                    user.setOtp(random + "_" + context + "_" + createAt);
                    userRepository.save(user);
                    notificationService.sendAuthOtp(random, user.getEmail());
                    data.put("context", context);
                } else {
                    try {
                        String res = sMSUtil.sendOtp(
                            sMSUtil.sendOtp(user.getPhoneDialCode().replace("+", "") + user.getPhoneNumber()));
                        if (res.equals("error")) {
                            return ResponseWrapper.response400("phoneNumber not found", "phoneNumber");
                        } else {
                            JSONObject jo = new JSONObject(res);
                            JSONObject oneTimePassword = jo.getJSONObject("oneTimePassword");
                            String id = oneTimePassword.getString("Id");
                            data.put("context", id);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage());
                        return ResponseWrapper.response400("phoneNumber not found", "phoneNumber");
                    }
                }
                user.setFailureCount(0);
                user.setLastFailureTime(null);
                this.userRepository.save(user);
                data.put("userId", user.getId());
                data.put("email", user.getEmail());
                data.put("roleId", user.getRole().getId());
                data.put("roleName", user.getRole().getRoleName());
                return ResponseWrapper.response(data);
            }
            if (Objects.isNull(lastFailureTime) || lastFailureTime.isBefore(LocalDateTime.now().minusMinutes(failureCheckMinutes)))
                user.setFailureCount(1);
            else
                user.setFailureCount(user.getFailureCount() + 1);
            user.setLastFailureTime(LocalDateTime.now());
            userRepository.save(user);
            if (user.getFailureCount() == maxFailureAttempts)
                return ResponseWrapper.response400("Your account is temporarily blocked due to multiple failed attempts. " +
                    "Please try after " + (LocalDateTime.now().minusMinutes(accountSuspendMinutes).until(lastFailureTime, ChronoUnit.MINUTES) + 1)
                    + " minutes", "password");
            return ResponseWrapper.response400("incorrect password", "password");
        }
    }

    @Override
    public ResponseEntity<?> resendOtp(ResendOtpDto createAuthTokenDto) {
        String email = createAuthTokenDto.getEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseWrapper.response400("user not found", "email");
        } else {
            Map<String, Object> data = new HashMap<>();
            if (!user.getInvitationStatus().equals(Constant.REGISTERED.toString())) {
                return ResponseWrapper.response400("User either blocked/not registered", "email");
            }
            String otp = user.getOtp();
            if (otp == null || otp.isEmpty()) {
                return ResponseWrapper.response400("resend otp work only during login context", "otp");
            }
            String[] parts = (otp).split("_");
            String context = parts[1];
            Integer random = utility.getOtp();
            long createAt = new Date().toInstant().toEpochMilli();
            if (createAuthTokenDto.getIsEmail() != null && createAuthTokenDto.getIsEmail()) {
                user.setOtp(random + "_" + context + "_" + createAt);
                userRepository.save(user);
                notificationService.sendAuthOtp(random, user.getEmail());
            } else {
                try {
                    String res = sMSUtil
                        .sendOtp(sMSUtil.sendOtp(user.getPhoneDialCode().replace("+", "") + user.getPhoneNumber()));
                    if (res.equals("error")) {
                        return ResponseWrapper.response400("phoneNumber not found", "phoneNumber");
                    } else {
                        JSONObject jo = new JSONObject(res);
                        JSONObject oneTimePassword = jo.getJSONObject("oneTimePassword");
                        String id = oneTimePassword.getString("Id");
                        data.put("context", id);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                    return ResponseWrapper.response400("phoneNumber not found", "phoneNumber");
                }
            }
            data.put("userId", user.getId());
            data.put("email", user.getEmail());
            return ResponseWrapper.response(data);
        }
    }

}
