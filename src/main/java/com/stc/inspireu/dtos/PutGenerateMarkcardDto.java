package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class PutGenerateMarkcardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jsonMarkCard;

}
