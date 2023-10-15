package com.stc.inspireu.controllers;

import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.naming.SizeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandles;
import java.util.Map;

@ControllerAdvice
@RestController
@RequiredArgsConstructor
public class GlobalExceptionController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PATH = "/error";

    private final ErrorAttributes errorAttributes;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String requestSize;

    @RequestMapping(value = PATH)
    public ResponseEntity<Object> error(HttpServletRequest request, HttpServletResponse response,
                                        WebRequest webRequest) {
        Map<String, Object> errorAttributeMap = getErrorAttributes(webRequest);
        for (Map.Entry<String, Object> entry : errorAttributeMap.entrySet()) {
        }

        if ((int) errorAttributeMap.get("status") > 499) {
            return ResponseWrapper.response("Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return ResponseWrapper.response((String) errorAttributeMap.get("message"),
                (String) errorAttributeMap.get("error"), HttpStatus.valueOf((int) errorAttributeMap.get("status")));
        }
    }

    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults()
            .including(ErrorAttributeOptions.Include.MESSAGE);
        return this.errorAttributes.getErrorAttributes(webRequest, options);
    }

    @ExceptionHandler(value = {MaxUploadSizeExceededException.class, MultipartException.class,
        SizeLimitExceededException.class})
    public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
        return ResponseWrapper.response400("The total size of all\n" +
            "  attachments combined should not exceed\n" +
            "  " + requestSize + ".", "file");
    }

}
