package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class PostMessageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "should be valid format")
    private String messageName;

    @NotBlank(message = "messageType required")
    private String messageType;

    @NotBlank(message = "messageDescription required")
    private String messageDescription;
}
