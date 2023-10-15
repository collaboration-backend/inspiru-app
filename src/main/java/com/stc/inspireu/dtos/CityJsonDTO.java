package com.stc.inspireu.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CityJsonDTO {
    private Long id;
    private String name;
    private String nameAr;

    private String label;

    private String labelAr;
    private String stateCode;
    private String countryCode;

    private Long value;

    public CityJsonDTO(Long id, String name, String nameAr, String stateCode, String countryCode) {
        this.id = id;
        this.name = name;
        this.nameAr = nameAr;
        this.stateCode = stateCode;
        this.countryCode = countryCode;
    }

    public String getLabel() {
        return name;
    }

    public String getLabelAr() {
        return nameAr;
    }

    public Long getValue() {
        return id;
    }
}
