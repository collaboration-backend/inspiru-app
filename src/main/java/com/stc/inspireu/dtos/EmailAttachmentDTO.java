package com.stc.inspireu.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailAttachmentDTO {
    private Long id;

    private String name;

    private String path;
}
