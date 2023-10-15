package com.stc.inspireu.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class StartupFeedbackFormsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long startupId;

    private String startupName;

    private Long feedbackId;

    private String feedbackName;

    private List<UserDto> startupMembers;

    private Long submittedOn;

    private String status;

    private String jsonForm;

    private Long intakeProgramId;

    private String startupProfileInfoJson;

    public StartupFeedbackFormsDto() {
        super();
    }
}
