package com.stc.inspireu.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stc.inspireu.dtos.CountryDTO;
import com.stc.inspireu.dtos.CountryJsonDTO;
import com.stc.inspireu.services.CountryService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountryServiceImpl implements CountryService {

    @Override
    public CountryDTO findByCountryCode(String code) throws Exception {
        return readFromJson().stream().filter(c -> c.getIso2().equals(code)).findFirst()
            .map(c -> new CountryDTO(c.getIso2(), c.getIso2(), c.getName(), c.getNameAr(), c.getName())).orElse(new CountryDTO());
    }

    private List<CountryJsonDTO> readFromJson() throws Exception {
        ClassPathResource countryJson = new ClassPathResource("countries.json");
        return new Gson().fromJson(new String(FileCopyUtils.copyToByteArray(countryJson.getInputStream()), StandardCharsets.UTF_8),
            new TypeToken<ArrayList<CountryJsonDTO>>() {
            }.getType());
    }

    @Override
    public List<CountryDTO> findAll() throws Exception {
        List<CountryDTO> countryDTOS = readFromJson().stream().map(c -> new CountryDTO(c.getIso2(), c.getIso2(), c.getName(), c.getNameAr(), c.getName())).collect(Collectors.toList());
        countryDTOS.add(new CountryDTO("other", "other", "Other", "أخرى", "Other"));
        return countryDTOS;
    }

    @Override
    public List<CountryDTO> findAlByCodes(List<String> codes) throws Exception {
        List<CountryDTO> countryDTOS = readFromJson()
            .stream()
            .filter(c -> codes.contains(c.getIso2()))
            .map(c -> new CountryDTO(c.getIso2(), c.getIso2(), c.getName(), c.getNameAr(), c.getName())).collect(Collectors.toList());
        countryDTOS.add(new CountryDTO("other", "other", "Other", "أخرى", "Other"));
        return countryDTOS;
    }

}
