package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.AssignmentManagementDto;
import com.stc.inspireu.models.User;

@Transactional
public interface AssignmentRepositoryCustom {
	List<AssignmentManagementDto> getAssignments(User user, Long academyRoomId, Long workshopSessionId, Pageable paging,
			String filterBy, String searchBy);

}
