package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.dtos.FeedbackFormManagementDto;
import com.stc.inspireu.dtos.StartupFeedbackFormsDto;
import com.stc.inspireu.models.User;

@Transactional
public interface FeedbackRepositoryCustom {
	List<StartupFeedbackFormsDto> getStartupFeedbacks(Long refFeedbackId, Long workshopSessionId, Long academyRoomId,
			Pageable paging, String filterBy, String searchBy);

	List<FeedbackFormManagementDto> getFeedbacks(User user, Long academyRoomId, Long workshopSessionId, Pageable paging,
			String filterBy, String searchBy);
}
