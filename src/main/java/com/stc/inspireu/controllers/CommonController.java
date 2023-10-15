package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.KeyValue;
import com.stc.inspireu.services.*;
import com.stc.inspireu.utils.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CommonService commonService;

    private final UserService userService;

    private final RevenueModelService revenueModelService;

    private final StatusService statusService;

    private final SegmentService segmentService;

    private final ReasonDropdownService reasonDropdownService;

    private final KeyValueService keyValueService;

    private final EmailUtil emailUtil;

    private final SMSUtil sMSUtil;

    @GetMapping("/api/${api.version}/status")
    public ResponseEntity<Object> apiStatus(HttpServletRequest httpServletRequest) {
        LOGGER.info("apiStatus");
        return ResponseWrapper.response("ok", HttpStatus.OK);
    }

    @GetMapping("/api/${api.version}/dropdown/revenueModels")
    public ResponseEntity<Object> dropdownRevenueModels(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/revenueModels");
        return ResponseWrapper.response(revenueModelService.findAllRevenueModel());

    }

    @GetMapping("/api/${api.version}/dropdown/segments")
    public ResponseEntity<Object> dropdownSegments(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/segments");
        return ResponseWrapper.response(segmentService.findAllSegments());
    }

    @GetMapping("/api/${api.version}/dropdown/status")
    public ResponseEntity<Object> dropdownStatus(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/status");
        return ResponseWrapper.response(statusService.findAllStatus());
    }

    @GetMapping("/api/${api.version}/dropdown/reasons")
    public ResponseEntity<Object> dropdownReason(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/status");
        return ResponseWrapper.response(reasonDropdownService.findAllReasonDropdown());
    }

    @GetMapping("/api/${api.version}/dropdown/startups")
    public ResponseEntity<?> dropdownStartup(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/startups");
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return commonService.dropdownStartups(currentUserObject);
    }

    @GetMapping("/api/${api.version}/dropdown/formTemplates/{formTemplateType}")
    public ResponseEntity<?> dropdownFormTemplates(HttpServletRequest httpServletRequest,
                                                   @PathVariable String formTemplateType) {
        LOGGER.info("dropdown/formTemplates");
        if (Arrays.asList(ConstantUtility.getFormTemplateTypeList()).contains(formTemplateType)) {
            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");
            return commonService.dropdownFormTemplates(currentUserObject, formTemplateType,
                Constant.PUBLISHED.toString());
        } else {
            return ResponseWrapper.response("invalid formType", "formType", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/api/${api.version}/dropdown/trainers")
    public ResponseEntity<Object> dropdownTrainers(HttpServletRequest httpServletRequest) {
        LOGGER.info("dropdown/startups");
        List<Object> list1 = userService.dropdownTrainers(RoleName.Value.ROLE_COACHES_AND_TRAINERS.toString());
        return ResponseWrapper.response(list1);
    }

    @GetMapping("/api/${api.version}/formTemplates/{formTemplateType}/basic")
    public ResponseEntity<Object> basicTemplates(HttpServletRequest httpServletRequest,
                                                 @PathVariable String formTemplateType) {

        if (formTemplateType.equals("dueDiligence")) {
            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("name", "Basic due dligence template");
                    put("description", "");
                    put("jsonForm", ConstantUtility.DUE_DILIGENCE_BASIC_TEMPLATE);
                }
            };
            return ResponseWrapper.response(data);
        } else if (formTemplateType.equals("survey")) {
            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("name", "Basic survey template");
                    put("description", "");
                    put("jsonForm", ConstantUtility.SURVEY_BASIC_TEMPLATE);
                }
            };
            return ResponseWrapper.response(data);
        } else if (formTemplateType.equals("feedback")) {
            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("name", "Basic feedback template");
                    put("description", "");
                    put("jsonForm", ConstantUtility.FEEDBACK_BASIC_TEMPLATE);
                }
            };
            return ResponseWrapper.response(data);
        } else if (formTemplateType.equals("profileCard")) {
            Map<String, Object> data = new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("name", "Basic profile card template");
                    put("description", "");
                    put("jsonForm", ConstantUtility.PROFILE_CARD_BASIC_TEMPLATE);
                }
            };
            return ResponseWrapper.response(data);
        } else {
            return ResponseWrapper.response400("invalid formTemplateType", "formTemplateType");
        }

    }

    @GetMapping("/test/mail/{email}")
    public ResponseEntity<Object> testmail(HttpServletRequest httpServletRequest, @PathVariable String email) {
        LOGGER.info("testmail");

        MailMetadata mailMetadata = new MailMetadata();

        Map<String, Object> props = new HashMap<>();

        String link = "/test/mail/" + email;

        props.put("inviteLink", link);
        props.put("toMail", email);

        mailMetadata.setFrom("");
        mailMetadata.setTo(email);
        mailMetadata.setProps(props);
        mailMetadata.setSubject("test mail");
        String h = "<p>Hi <span class=\"mention\" data-index=\"1\" data-denotation-char=\"#\" data-id=\"2\" data-value=\"STARTUP_EMAIL\">﻿<span\r\n"
            + "            contenteditable=\"false\"><span class=\"ql-mention-denotation-char\">#</span>STARTUP_EMAIL</span>﻿</span> ,</p>\r\n"
            + "<p>Welcome to Inspireu program</p>\r\n"
            + "<p><span class=\"mention\" data-index=\"0\" data-denotation-char=\"#\" data-id=\"1\" data-value=\"STARTUP_NAME\">﻿<span\r\n"
            + "            contenteditable=\"false\"><span class=\"ql-mention-denotation-char\">#</span>STARTUP_NAME</span>﻿</span> </p>\r\n"
            + "<p>Thanks Regards</p>";

        Map<String, String> d = new HashMap<>();

        d.put("STARTUP_EMAIL", email);
        d.put("STARTUP_NAME", "bbbbbb");

        h = emailUtil.replaceEmailToken(h, d);

        String emailHtml = emailUtil.getTemplateHtml("", h, "", null, null);

        mailMetadata.setTemplateString(emailHtml);

        try {
            emailUtil.sendEmail(mailMetadata);
            LOGGER.debug("mail sending complete");
            return ResponseWrapper.response("ok");
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
            return ResponseWrapper.response(e.getMessage());
        }

    }

    @GetMapping("/api/${api.version}/initData")
    public ResponseEntity<Object> allBasicTemplates(HttpServletRequest httpServletRequest) {

        Map<String, Object> data = new HashMap<String, Object>();

        Map<String, Object> d1 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic due dligence template");
                put("description", "");
                put("jsonForm", ConstantUtility.DUE_DILIGENCE_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d2 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic survey template");
                put("description", "");
                put("jsonForm", ConstantUtility.SURVEY_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d3 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic feedback template");
                put("description", "");
                put("jsonForm", ConstantUtility.FEEDBACK_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d4 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic profile card template");
                put("description", "");
                put("jsonForm", ConstantUtility.PROFILE_CARD_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d5 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic progress report template");
                put("description", "");
                put("jsonForm", ConstantUtility.PROGRESS_REPORT_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d6 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic assessment form template");
                put("description", "");
                put("jsonForm", ConstantUtility.ASSESMENT_FROM_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d7 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic bootcamp form template");
                put("description", "");
                put("jsonForm", ConstantUtility.BOOTCAMP_FROM_BASIC_TEMPLATE);
            }
        };

        Map<String, Object> d8 = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("name", "Basic registration form template");
                put("description", "");
                put("jsonForm", ConstantUtility.REGISTRATION_FROM_BASIC_TEMPLATE);
            }
        };

        data.put("dueDiligence", d1);
        data.put("survey", d2);
        data.put("feedback", d3);
        data.put("profileCard", d4);

        data.put("progressReport", d5);
        data.put("assessmentForm", d6);
        data.put("bootcampForm", d7);
        data.put("registrationForm", d8);

        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();

        try {
            String[] ll = ConstantUtility.getEmailtemplateKeywords();

            for (int i = 0; i < ll.length; i++) {
                Map<String, Object> a = new HashMap<String, Object>();
                a.put("key", i + 1);
                a.put("value", ll[i]);
                a.put("label", ll[i]);
                ls.add(a);
            }

        } catch (Exception e) {

        }

        data.put("emailTokens", ls);

        KeyValue filesAllowded = keyValueService.findKeyValueByName(Constant.FILES_ALLOWDED.toString());

        data.put("fileTypes", "");

        if (filesAllowded != null) {

            data.put("fileTypes", filesAllowded.getValueName());
        }

        KeyValue fileSize = keyValueService.findKeyValueByName(Constant.FILE_SIZE.toString());

        data.put("fileSize", 0);

        if (fileSize != null) {
            data.put("fileSize", Integer.parseInt(fileSize.getValueName()));
        }
        return ResponseWrapper.response(data);
    }

    @GetMapping("/test/otp/{phone}")
    @ResponseBody
    public ResponseEntity<?> otpT(HttpServletRequest httpServletRequest, @PathVariable("phone") String phone) {
        String res = sMSUtil.sendOtp(phone);
        return ResponseWrapper.response(res);
    }

    @GetMapping("/test/otp/ver/{id}/{otp}")
    @ResponseBody
    public ResponseEntity<?> otpV(HttpServletRequest httpServletRequest, @PathVariable("id") String id,
                                  @PathVariable("otp") String otp) {
        String res = sMSUtil.validateOtp(id, otp);
        return ResponseWrapper.response(res);
    }
}
