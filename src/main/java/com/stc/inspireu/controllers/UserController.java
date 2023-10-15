package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.ChangePasswordDto;
import com.stc.inspireu.dtos.GetManagementProfileDto;
import com.stc.inspireu.dtos.ManagementProfileDto;
import com.stc.inspireu.dtos.UpdateNotificationDto;
import com.stc.inspireu.models.User;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/${api.version}")
@RequiredArgsConstructor
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final Utility utility;

    @PostMapping("changePassword")
    public ResponseEntity<Object> changePassword(HttpServletRequest httpServletRequest,
                                                 @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (changePasswordDto.getPassword().equals(changePasswordDto.getConfirmPassword())) {
            Object obj = userService.changePassword(currentUserObject, changePasswordDto);
            if (obj != null) {
                return ResponseWrapper.response(null, "password reset done");
            } else {
                return ResponseWrapper.response400("invalid oldPassword", "oldPassword");
            }
        } else {
            return ResponseWrapper.response400("confirmPassword should be equal to password", "confirmPassword");
        }
    }

    @GetMapping("notifications")
    public ResponseEntity<Object> getNotifications(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = userService.getNotification(currentUserObject);

        return ResponseWrapper.response(data);
    }

    @PutMapping("notifications")
    public ResponseEntity<Object> updateNotification(HttpServletRequest httpServletRequest,
                                                     @Valid @RequestBody UpdateNotificationDto updateNotificationDto, BindingResult bindingResult) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {
            return ResponseWrapper.response(null, "notification settings updated");
        }

    }

    @GetMapping("management/myProfile")
    public ResponseEntity<Object> getMyProfile(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        GetManagementProfileDto getManagementProfileDto = userService.getManagementMyProfile(currentUserObject);
        return ResponseWrapper.response(getManagementProfileDto);
    }

    @PutMapping(value = "management/myProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateMyProfile(HttpServletRequest httpServletRequest,
                                                  @Valid @ModelAttribute ManagementProfileDto managementProfileDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Map<String, Object> fileCheck = utility.checkFile(managementProfileDto.getProfilePic());

            if (!(boolean) fileCheck.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck.get("error"), "profilePic");
            }

            Map<String, Object> fileCheck1 = utility.checkFile(managementProfileDto.getSignature());

            if (!(boolean) fileCheck1.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck1.get("error"), "signature");
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            User user = userService.updateManagementMyProfile(currentUserObject, managementProfileDto);

            if (user == null) {
                return ResponseWrapper.response400("email is already registered", "email");
            }

            Map<String, Object> claims = new HashMap<String, Object>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());

            String jwt = jwtUtil.generateAuthToken(claims);

            Map<String, String> data = new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;

                {
                    put("memberName", managementProfileDto.getMemberName());
                    put("phoneNumber", managementProfileDto.getPhoneNumber());
                    put("registratedEmailAddress", managementProfileDto.getRegistratedEmailAddress());
                }
            };

            if (managementProfileDto.getRegistratedEmailAddress() != null) {
                data.put("newAuthToken", jwt);
            }

            return ResponseWrapper.response(data, "management my profile update success");

        }

    }

    @PutMapping(value = "management/judges/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateJudgeSignature(HttpServletRequest httpServletRequest,
                                                       @Valid @RequestParam MultipartFile signaturePic) {
        if (signaturePic == null) {
            return ResponseWrapper.response400("signaturePic not be null", "sidnaturePic");
        } else {

            Map<String, Object> fileCheck1 = utility.checkFile(signaturePic);

            if (!(boolean) fileCheck1.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck1.get("error"), "signature");
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            return userService.updateSignature(currentUserObject, signaturePic);

        }
    }

    @GetMapping("userContext")
    public ResponseEntity<Object> userContext(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Object data = userService.userContext(currentUserObject);

        return ResponseWrapper.response(data);
    }

}
