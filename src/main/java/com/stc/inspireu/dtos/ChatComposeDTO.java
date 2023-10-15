package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChatComposeDTO {

    private Long userId;

    private Long recipientId;

    @NotEmpty(message = "Message is required")
    private String message;

}
