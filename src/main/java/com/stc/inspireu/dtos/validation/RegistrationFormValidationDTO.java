package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RegistrationFormValidationDTO {

    private List<RegistrationFormValidationDTO_form> form;

    private RegistrationFormValidationDTO_metadata metadata;

    private String createdOn;

    @Override
    public boolean equals(Object obj) {
        RegistrationFormValidationDTO that = (RegistrationFormValidationDTO) obj;
        return Objects.equals(this.form, that.form) && Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(form, metadata);
    }
}

