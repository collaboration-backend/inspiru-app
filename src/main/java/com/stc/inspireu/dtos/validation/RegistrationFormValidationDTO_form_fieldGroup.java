package com.stc.inspireu.dtos.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class RegistrationFormValidationDTO_form_fieldGroup {

    private String key;

    private String type;

    private Object defaultValue;

    private RegistrationFormValidationDTO_form_fieldGroup_templateOptions templateOptions;

    private List<RegistrationFormValidationDTO_form_fieldGroup> fieldGroup;

    private List<RegistrationFormValidationDTO_form_fieldGroup> fields;

    private Map<String, List<RegistrationFormValidationDTO_form_fieldGroup>> subForms = new HashMap<>();

    public Object getDefaultValue() {
        if (defaultValue instanceof Double)
            defaultValue = (Integer) ((Double) defaultValue).intValue();
        else if (defaultValue instanceof List) {
            defaultValue = (List<String>) ((List<?>) defaultValue).stream().map(val -> {
                Integer finalVal = null;
                if (val instanceof Double)
                    finalVal = (Integer) ((Double) val).intValue();
                return Objects.nonNull(finalVal) ? String.valueOf(finalVal) : val.toString();
            }).collect(Collectors.toList());
        }
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationFormValidationDTO_form_fieldGroup)) return false;
        RegistrationFormValidationDTO_form_fieldGroup that = (RegistrationFormValidationDTO_form_fieldGroup) o;
        return Objects.equals(key, that.key) && Objects.equals(type, that.type) && Objects.equals(templateOptions, that.templateOptions) && Objects.equals(fieldGroup, that.fieldGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, templateOptions, fieldGroup);
    }
}
