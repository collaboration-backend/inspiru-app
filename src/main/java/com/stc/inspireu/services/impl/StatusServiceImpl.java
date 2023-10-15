package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.Status;
import com.stc.inspireu.services.StatusService;
import com.stc.inspireu.repositories.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;


    @Override
    public Iterable<Status> findAllStatus() {
        return statusRepository.findAll();
    }
}
