package com.stc.inspireu.exceptions;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.joda.time.IllegalFieldValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class AppExceptionHandler {

    private final MessageSource messageSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleValidation(ConstraintViolationException constraintViolationException) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", null);
        if (constraintViolationException.getMessage().contains(":"))
            map.put("message", constraintViolationException.getMessage().split(":")[1].trim());
        else
            map.put("message", constraintViolationException.getMessage());
        // map.put("error", errorObj(((FieldError) objectError).getField()));
        return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalFieldValueException.class, MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<?> handleDateConversionException(Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", null);
        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            message = StringUtils.join(exception.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList()), ",");
        }
        map.put("message", message);
        return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomRunTimeException.class)
    public ResponseEntity<?> handleCustomException(CustomRunTimeException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", null);
        map.put("message", e.getMessage());
        return new ResponseEntity<Object>(map, e.getHttpStatus());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<?> handleItemNotFoundException(ItemNotFoundException e) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("message", messageSource.getMessage("item.not.found", new String[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e2) {
            LOGGER.error("", e2);
        }
        return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runTimeException(RuntimeException e) {
        if (!(e instanceof HttpMessageNotWritableException))
            LOGGER.error("Exception", e);
        Map<String, Object> map = new HashMap<>();
        map.put("data", null);
        map.put("message", e.getMessage());
        return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
    }
}
