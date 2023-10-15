package com.stc.inspireu.services;

import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.CreateIntakeProgramRegistrationDto;
import com.stc.inspireu.dtos.OpenEventBookingDto;
import com.stc.inspireu.dtos.PostIntakeProgramRegistrationDto;

public interface PublicFormService {

    Object intakeProgramRegistration(CurrentUserObject currentUserObject,
                                     PostIntakeProgramRegistrationDto postIntakeProgramRegistrationDto);

    ResponseEntity<?> getFormInfo(CurrentUserObject currentUserObject, Long formId, String formTemplateType);

    ResponseEntity<Object> _intakeProgramRegistration(
        CreateIntakeProgramRegistrationDto createIntakeProgramRegistrationDto);

    ResponseEntity<Object> openEventBooking(CurrentUserObject currentUserObject,
                                            OpenEventBookingDto openEventBookingDto);

    ResponseEntity<Object> getOpenEventBooking(CurrentUserObject currentUserObject);

    ResponseEntity<Object> getOpenEventDay(CurrentUserObject currentUserObject, Long openEventId, Long dateInMilli,
                                           String timezone);

    ResponseEntity<?> locationLookup(Long cityId, String iso2CountryCode, String stateCode) throws Exception;

}
