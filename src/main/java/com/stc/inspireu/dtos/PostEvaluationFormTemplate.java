package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostEvaluationFormTemplate implements Serializable {

    private String evaluationTemplateName;

    private Long intakePgmId;

    private String evaluationPhase;

    private String formJson;
}
