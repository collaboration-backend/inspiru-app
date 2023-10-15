package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class VerifyAuthTokenDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "authToken required")
    private String authToken;

}
