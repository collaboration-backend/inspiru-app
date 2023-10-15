package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Data
public class PostStartupOnboardingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "require atleat one startup email")
    private Set<@NotNull String> startupEmail;

    private String emailsAndStartups;
}
