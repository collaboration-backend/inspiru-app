package com.stc.inspireu.services.impl;

import com.stc.inspireu.dtos.RegistrationFormDto;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.models.RegistrationForm;
import com.stc.inspireu.repositories.RegistrationFormRepository;
import com.stc.inspireu.services.RegistrationFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationFormServiceImpl implements RegistrationFormService {

    private final RegistrationFormRepository formRepository;

    @Override
    public RegistrationFormDto findFormById(Long id) {
        RegistrationForm form = formRepository.findById(id).orElseThrow(() -> ItemNotFoundException.builder("Form").build());
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setFormJson(form.getJsonForm());
        dto.setRegistrationFormName(form.getFormName());
        return dto;
    }
}
