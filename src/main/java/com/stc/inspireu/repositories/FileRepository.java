package com.stc.inspireu.repositories;

import com.stc.inspireu.models.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FileRepository extends PagingAndSortingRepository<File, Long> {

	Page<File> findByName(String name, Pageable paging);

	@Query("select t from File t where t.status = :status")
	Page<File> findAllUploadedByManagement(Pageable paging, @Param("status") String status);

	@Query("select c from File c where c.name like %:c% AND c.status= :status")
	Page<File> findAllUploadedByManagementAndName(@Param("c") String c, @Param("status") String status,
			Pageable paging);

	Page<File> findByNameContainingIgnoreCase(String name, Pageable paging);

	@Query("select u from File u where u.createdUser is not null and lower(u.createdUser.alias) like %:filterKeyword%")
	public Page<File> getFileByKeyword(String filterKeyword, Pageable paging);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM File e WHERE e.id = :fileId")
	int removeFileById(@Param("fileId") Long fileId);

	@Query("select t from File t")
	Page<File> findAllUploadedByManagement(Pageable paging);

	Page<File> findByIntakeProgram_IdAndStatusAndNameContainingIgnoreCase(Long i, String string, String name,
			Pageable paging);

	Page<File> findByIntakeProgram_IdAndStatus(Long i, String string, Pageable paging);

}
