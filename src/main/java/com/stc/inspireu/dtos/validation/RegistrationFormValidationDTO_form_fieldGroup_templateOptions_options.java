package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options {

    private String key;

    private String label;

    private String value;

    private String subForm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options)) return false;
        RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options that = (RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options) o;
        return Objects.equals(key, that.key) && Objects.equals(label, that.label) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, label, value);
    }
}
