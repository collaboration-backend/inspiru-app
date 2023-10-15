package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class TestSampleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "username required", groups = {ValidationWithoutAge.class, DefaultValidator.class})
    private String username;

    private String email;

    private String password;

    @Min(value = 18, message = "You have to be 18 to drive a car", groups = {ValidationWithoutUsername.class,
        DefaultValidator.class})
    private int age;

    public interface ValidationWithoutUsername {
    }

    public interface ValidationWithoutAge {
    }

    public interface DefaultValidator {
    }
}
