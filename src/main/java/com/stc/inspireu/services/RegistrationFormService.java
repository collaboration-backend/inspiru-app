package com.stc.inspireu.services;

import com.stc.inspireu.dtos.RegistrationFormDto;

public interface RegistrationFormService {

    RegistrationFormDto findFormById(Long id);
}
