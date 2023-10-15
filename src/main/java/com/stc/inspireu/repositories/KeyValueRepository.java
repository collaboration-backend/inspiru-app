package com.stc.inspireu.repositories;

import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.KeyValue;

@Transactional
public interface KeyValueRepository extends PagingAndSortingRepository<KeyValue, String> {

	KeyValue findByKeyName(String string);

	long countByKeyNameIn(Set<String> kv);

}
