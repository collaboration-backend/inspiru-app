package com.stc.inspireu.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.stc.inspireu.models.TrainingSession;

public interface TrainingSessionRepository extends PagingAndSortingRepository<TrainingSession, Long> {

}
