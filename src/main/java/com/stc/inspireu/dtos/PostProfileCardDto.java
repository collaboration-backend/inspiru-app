package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostProfileCardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String profileCardName;

    @NotNull
    private String jsonForm;

    private Long refProfileCardId;
}
