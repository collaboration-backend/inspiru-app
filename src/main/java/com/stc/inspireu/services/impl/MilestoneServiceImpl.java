package com.stc.inspireu.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.services.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.MileStoneDto;
import com.stc.inspireu.models.Milestone;
import com.stc.inspireu.repositories.IntakeProgramRepository;
import com.stc.inspireu.repositories.MilestoneRepository;
import com.stc.inspireu.repositories.UserRepository;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    @Autowired
    MilestoneRepository milestoneRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    IntakeProgramRepository intakeProgramRepository;

    @Transactional
    @Override
    public Boolean createMileStone(CurrentUserObject currentUserObject, MileStoneDto mileStoneDto, String status) {
        try {
            Milestone milestone = new Milestone();
            milestone.setMilestoneName(mileStoneDto.getMileStoneName());
            milestone.setMilestoneCondition(milestone.getMilestoneCondition());
            milestone.setCreatedUser(userRepository.findById(currentUserObject.getUserId()).get());
            milestone.setMilestoneNumber(mileStoneDto.getMileStoneNumber());
            milestone.setIntakeProgram(intakeProgramRepository.findById(mileStoneDto.getIntakePgmId()).get());
            milestone.setApplicable(mileStoneDto.getApplicableTo());
            milestone.setMilestoneCondition(mileStoneDto.getCondition());
            milestone.setStatus(status);
            milestoneRepository.save(milestone);
            return true;
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @Override
    public List<MileStoneDto> milestoneList(CurrentUserObject currentUserObject, Integer pageNo, Integer pageSize) {

        Pageable paging = PageRequest.of(pageNo, pageSize);
        Iterable<Milestone> list = milestoneRepository.findAll(paging);
        List<MileStoneDto> mileStoneList = new ArrayList<>();
        for (Milestone milestone : list) {
            MileStoneDto mileStoneDto = new MileStoneDto();
            mileStoneDto.setMileStoneName(milestone.getMilestoneName());
            mileStoneDto.setMileStoneNumber(milestone.getMilestoneNumber());
            mileStoneDto.setStatus(milestone.getStatus());
            mileStoneDto.setCreatedBy(milestone.getCreatedUser().getAlias());
            mileStoneDto.setMileStoneNumber(milestone.getMilestoneNumber());
            mileStoneDto.setInTakePgmName(milestone.getIntakeProgram().getProgramName());
            mileStoneList.add(mileStoneDto);
        }
        return mileStoneList;
    }

    @Transactional
    @Override
    public Boolean updateMileStoneById(CurrentUserObject currentUserObject, MileStoneDto mileStoneDto, Long id) {
        try {
            Optional<Milestone> milestone = milestoneRepository.findById(id);
            if (milestone.isPresent()) {
                milestone.get().setMilestoneName(mileStoneDto.getMileStoneName());
                milestone.get().setMilestoneCondition(mileStoneDto.getCondition());
                milestone.get().setMilestoneNumber(mileStoneDto.getMileStoneNumber());
                milestone.get().setIntakeProgram(intakeProgramRepository.findById(mileStoneDto.getIntakePgmId()).get());
                milestone.get().setApplicable(mileStoneDto.getApplicableTo());
                milestone.get().setMilestoneCondition(mileStoneDto.getCondition());
                milestoneRepository.save(milestone.get());
                return true;
            }
            return false;
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @Override
    public Boolean updateStatus(Long id, String status) {

        Milestone milestone = milestoneRepository.findById(id).orElseThrow(() -> new CustomRunTimeException("Milestone not found"));
        milestone.setStatus(status);
        Milestone result = milestoneRepository.save(milestone);
        return result.getStatus().equalsIgnoreCase(status);
    }
}
