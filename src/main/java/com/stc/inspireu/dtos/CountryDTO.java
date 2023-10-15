package com.stc.inspireu.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CountryDTO {

    private String key;

    private String value;

    private String label;

    private String labelAr;

    private String countryName;

}
