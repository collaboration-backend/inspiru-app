package com.stc.inspireu.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stc.inspireu.dtos.CityJsonDTO;
import com.stc.inspireu.services.CityService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityServiceImpl implements CityService {

    @Override
    public CityJsonDTO findById(Long id) throws Exception {
        if (id.equals(0L))
            return new CityJsonDTO(0L, "Other", "أخرى", "", "");
        return readFromJson().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    private List<CityJsonDTO> readFromJson() throws Exception {
        ClassPathResource cityJson = new ClassPathResource("cities.json");
        return new Gson().fromJson(new String(FileCopyUtils.copyToByteArray(cityJson.getInputStream()), StandardCharsets.UTF_8),
            new TypeToken<ArrayList<CityJsonDTO>>() {
            }.getType());
    }

    @Override
    public List<CityJsonDTO> findByIds(List<Long> ids) throws Exception {
        List<CityJsonDTO> cities = new ArrayList<>();
        if (ids.contains(0L))
            cities.add(new CityJsonDTO(0L, "Other", "أخرى", "", ""));
        readFromJson().stream().filter(c -> ids.contains(c.getId())).forEach(cities::add);
        return cities;
    }

    @Override
    public List<CityJsonDTO> findByIdAndCountryCodeAndStateCode(Long cityId, String iso2CountryCode, String stateCode) throws Exception {
        return readFromJson().stream()
            .filter(c -> c.getId().equals(cityId) && c.getCountryCode().equals(iso2CountryCode) && c.getStateCode().equals(stateCode))
            .collect(Collectors.toList());
    }

    @Override
    public List<CityJsonDTO> findByCountryCodeAndStateCode(String iso2CountryCode, String stateCode) throws Exception {
        return readFromJson().stream()
            .filter(c -> c.getCountryCode().equals(iso2CountryCode) && c.getStateCode().equals(stateCode))
            .collect(Collectors.toList());
    }

    @Override
    public List<CityJsonDTO> findByCountryCodeOrderByCityName(String iso2CountryCode) throws Exception {
        return readFromJson().stream()
            .filter(c -> c.getCountryCode().equals(iso2CountryCode))
            .collect(Collectors.toList());
    }
}
