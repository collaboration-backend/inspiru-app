package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.OpenEventSlot;

@Transactional
public interface OpenEventSlotRepository
		extends PagingAndSortingRepository<OpenEventSlot, Long>, JpaSpecificationExecutor<OpenEventSlot> {

	@Query(value = "select t from OpenEventSlot t where t.day BETWEEN :s AND :e and t.openEvent.id = :openEventId")
	Iterable<OpenEventSlot> getBookings(Date s, Date e, Long openEventId);

	List<OpenEventSlot> findByOpenEvent_IdAndEmail(Long openEventId, String email);
	boolean existsByOpenEvent_IdAndEmail(Long openEventId, String email);

	List<OpenEventSlot> findByOpenEvent_IdAndDay(Long openEventId, Date date);
	boolean existsByOpenEvent_IdAndDay(Long openEventId, Date date);

}
