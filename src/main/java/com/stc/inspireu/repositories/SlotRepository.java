package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.jpa.projections.ProjectId;
import com.stc.inspireu.models.Slot;

@Transactional
public interface SlotRepository extends PagingAndSortingRepository<Slot, Long> {

	@Query("select t from Slot t where t.startup.id = :startupId and t.id = :slotId")
	Slot findByStartupIdAndSlotId(@Param("startupId") Long startupId, @Param("slotId") Long slotId);

	List<ProjectId> findByStartup_IdAndSessionEndGreaterThanEqual(Long id, Date date);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM Slot e WHERE e.id = :slotId and e.startup.id = :startupId")
	void removeBySlotIdAndStartup(@Param("slotId") Long slotId, @Param("startupId") Long startupId);

	@Query(value = "select s from Slot s where s.startup.id = :startupId and ( (s.sessionStart <= :startDate AND s.sessionEnd >= :startDate) or (s.sessionStart <= :endDate AND s.sessionEnd >= :endDate))")
	List<Slot> getStartupScheduledSlots(@Param("startupId") Long startupId, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate);

}
