package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.RevenueModel;
import com.stc.inspireu.repositories.RevenueModelRepository;
import com.stc.inspireu.services.RevenueModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RevenueModelServiceImpl implements RevenueModelService {

    private final RevenueModelRepository revenueModelRepository;

    @Override
    public Iterable<RevenueModel> findAllRevenueModel() {
        return revenueModelRepository.findAll();
    }
}
