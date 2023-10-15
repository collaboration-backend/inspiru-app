package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostPartnerDto;
import com.stc.inspireu.services.PartnerService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.Map;

@RestController
@RequestMapping("/api/${api.version}/management")
@RequiredArgsConstructor
public class PartnerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Utility utility;

    private final PartnerService partnerService;

    @GetMapping("partners")
    public ResponseEntity<?> getPartners(HttpServletRequest httpServletRequest,
                                         @RequestParam(defaultValue = "0") Integer pageNo,
                                         @RequestParam(defaultValue = "50") Integer pageSize,
                                         @RequestParam(defaultValue = "asc") String sortDir) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        Pageable paging = PageRequest.of(pageNo, pageSize);

        return partnerService.getPartners(currentUserObject, paging);
    }


    @PostMapping(value = "partners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postPartner(HttpServletRequest httpServletRequest,
                                         @Valid @ModelAttribute PostPartnerDto postPartnerDto,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        } else {

            Map<String, Object> fileCheck = utility.checkFile(postPartnerDto.getLogo());

            if (!(boolean) fileCheck.get("isAllow")) {
                return ResponseWrapper.response400((String) fileCheck.get("error"), "logo");
            }

            CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest
                .getAttribute("currentUserObject");

            return partnerService.postPartner(currentUserObject, postPartnerDto);

        }

    }

}
