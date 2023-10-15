package com.stc.inspireu.repositories;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectProgressReportFile;
import com.stc.inspireu.models.ProgressReportFile;

@Transactional
public interface ProgressReportFileRepository extends PagingAndSortingRepository<ProgressReportFile, Long> {
	List<ProjectProgressReportFile> findByProgressReport_Id(Long progressReportId);

}
