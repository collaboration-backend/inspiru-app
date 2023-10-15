package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class ChatMessageDTO {

    private Long messageId;

    private Long senderId;

    private String sender;

    private String senderRole;

    private Long recipientId;

    private String recipient;

    private String recipientRole;

    private String message;

    private String createdOn;
}
