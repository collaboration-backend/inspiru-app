package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RegistrationFormValidationDTO_form {

    private String type;

    private int defaultValue;

    private List<RegistrationFormValidationDTO_form_fieldGroup> fieldGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationFormValidationDTO_form)) return false;
        RegistrationFormValidationDTO_form that = (RegistrationFormValidationDTO_form) o;
        return Objects.equals(type, that.type) && Objects.equals(fieldGroup, that.fieldGroup) && Objects.equals(this.defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fieldGroup, defaultValue);
    }

}
