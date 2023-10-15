package com.stc.inspireu.services;

import com.stc.inspireu.dtos.CountryDTO;

import java.util.List;

public interface CountryService {

    CountryDTO findByCountryCode(String code) throws Exception;
    List<CountryDTO> findAll() throws Exception;

    List<CountryDTO> findAlByCodes(List<String> codes)throws Exception;
}
