package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RegistrationFormValidationDTO_form_fieldGroup_templateOptions {

    private String _title;

    private Boolean required = Boolean.FALSE;

    private Boolean proLink = Boolean.FALSE;

    private Boolean hint = Boolean.FALSE;

    private Boolean multiple = Boolean.FALSE;

    private String hint_text;

    private List<RegistrationFormValidationDTO_form_fieldGroup_templateOptions_options> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationFormValidationDTO_form_fieldGroup_templateOptions)) return false;
        RegistrationFormValidationDTO_form_fieldGroup_templateOptions that = (RegistrationFormValidationDTO_form_fieldGroup_templateOptions) o;
        return Objects.equals(get_title(), that.get_title()) && Objects.equals(required, that.required)
            && Objects.equals(proLink, that.proLink) && Objects.equals(hint, that.hint)
            && Objects.equals(hint_text, that.hint_text) && Objects.equals(this.multiple, that.multiple);
    }

    @Override
    public int hashCode() {
        return Objects.hash(get_title(), required, proLink, hint, hint_text, multiple);
    }
}
