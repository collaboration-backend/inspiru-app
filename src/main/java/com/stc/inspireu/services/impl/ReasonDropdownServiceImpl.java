package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.ReasonDropdown;
import com.stc.inspireu.services.ReasonDropdownService;
import com.stc.inspireu.repositories.ReasonDropdownRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReasonDropdownServiceImpl implements ReasonDropdownService {

    private final ReasonDropdownRepository reasonDropdownRepository;

    @Override
    public Iterable<ReasonDropdown> findAllReasonDropdown() {
        return reasonDropdownRepository.findAll();
    }
}
