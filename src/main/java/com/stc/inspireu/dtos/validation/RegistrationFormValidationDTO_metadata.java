package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class RegistrationFormValidationDTO_metadata {

    private String name;

    private String description;

    private String language;

    public RegistrationFormValidationDTO_metadata() {
    }
    public RegistrationFormValidationDTO_metadata(String name, String description) {
        this.name = name;
        this.description = description;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationFormValidationDTO_metadata)) return false;
        RegistrationFormValidationDTO_metadata that = (RegistrationFormValidationDTO_metadata) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}
