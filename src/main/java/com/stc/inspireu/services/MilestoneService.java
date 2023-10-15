package com.stc.inspireu.services;

import java.util.List;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.MileStoneDto;

public interface MilestoneService {

	Boolean createMileStone(CurrentUserObject currentUserObject, MileStoneDto mileStoneDto, String Status);

	List<MileStoneDto> milestoneList(CurrentUserObject currentUserObject, Integer pageNo, Integer pageSize);

	Boolean updateMileStoneById(CurrentUserObject currentUserObject, MileStoneDto mileStoneDto, Long id);

	Boolean updateStatus(Long id, String Status);

}
