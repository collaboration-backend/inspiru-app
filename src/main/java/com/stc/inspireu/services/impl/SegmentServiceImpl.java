package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.Segment;
import com.stc.inspireu.services.SegmentService;
import com.stc.inspireu.repositories.SegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SegmentServiceImpl implements SegmentService {

    private final SegmentRepository segmentRepository;


    @Override
    public Iterable<Segment> findAllSegments() {
        return segmentRepository.findAll();
    }
}
