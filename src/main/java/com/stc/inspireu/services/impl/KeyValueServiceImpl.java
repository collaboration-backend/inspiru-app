package com.stc.inspireu.services.impl;

import com.stc.inspireu.models.KeyValue;
import com.stc.inspireu.services.KeyValueService;
import com.stc.inspireu.repositories.KeyValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyValueServiceImpl implements KeyValueService {

    private final KeyValueRepository keyValueRepository;

    @Override
    public KeyValue findKeyValueByName(String name) {
        return keyValueRepository.findByKeyName(name);
    }
}
