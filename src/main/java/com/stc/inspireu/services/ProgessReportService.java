package com.stc.inspireu.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.PostManagementProgressReportDto;
import com.stc.inspireu.dtos.PostProgressReportDto;
import com.stc.inspireu.dtos.ProgressReportDto;
import com.stc.inspireu.dtos.ProgressReportPostRequestDto;
import com.stc.inspireu.dtos.PutProgressReportDto;
import com.stc.inspireu.jpa.projections.ProjectProgressReportFile;

@SuppressWarnings("unused")
public interface ProgessReportService {
	Object createProgessReport(CurrentUserObject currentUserObject, ProgressReportPostRequestDto progressReportRequest);

	Object createProgessReportMonthly(CurrentUserObject currentUserObject, PostProgressReportDto postProgressReportDto);

	Object updateProgessReportMonthly(CurrentUserObject currentUserObject, PutProgressReportDto postProgressReportDto,
			Long progressReportId);

	ProgressReportDto getProgressReport(CurrentUserObject currentUserObject, Long progressReportId);

	List<ProjectProgressReportFile> getProgressReportFiles(CurrentUserObject currentUserObject, Long progressReportId);

	ResponseEntity<?> createManagementProgressReport(CurrentUserObject currentUserObject,
			PostManagementProgressReportDto progressReportMngtDto, String status);

	ResponseEntity<?> updateManagementProgressReport(CurrentUserObject currentUserObject,
			PostManagementProgressReportDto progressReportMngtDto, Long id);

	ResponseEntity<?> updateManagementProgressReportStatus(CurrentUserObject currentUserObject, String status, Long id);

	List<ProgressReportDto> getProgressReports(CurrentUserObject currentUserObject);

	ResponseEntity<?> getManagementProgressReportById(CurrentUserObject currentUserObject, Long progressReportId);

	ResponseEntity<?> deleteManagementProgressReportById(CurrentUserObject currentUserObject, Long progressReportId);

	Page<ProgressReportDto> getProgressReportMonthlySubmissions(CurrentUserObject currentUserObject,
			Long intakeProgramId, String searchBy, String filterBy, Pageable paging);

	Object putProgressReports(CurrentUserObject currentUserObject, PutProgressReportDto putProgressReportDto,
			Long progressReportId);

	ResponseEntity<?> getManagementProgressReports(CurrentUserObject currentUserObject, Pageable paging);

	ResponseEntity<?> checkProgressReports(CurrentUserObject currentUserObject);

	ResponseEntity<?> notifyManagement(CurrentUserObject currentUserObject);

}
