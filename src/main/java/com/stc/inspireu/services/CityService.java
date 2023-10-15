package com.stc.inspireu.services;

import com.stc.inspireu.dtos.CityJsonDTO;
import com.stc.inspireu.jpa.projections.ProjectCityByKeyValueLable;

import java.util.List;

public interface CityService {

    CityJsonDTO findById(Long id) throws Exception;

    List<CityJsonDTO> findByIds(List<Long> ids) throws Exception;

    List<CityJsonDTO> findByIdAndCountryCodeAndStateCode(Long cityId,
                                                         String iso2CountryCode, String stateCode) throws Exception;

    List<CityJsonDTO> findByCountryCodeAndStateCode(String iso2CountryCode, String stateCode) throws Exception;

    List<CityJsonDTO> findByCountryCodeOrderByCityName(String iso2CountryCode) throws Exception;
}
