package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class StartupEvaluationSummaryEvaluatorDTO {

    private Long evaluatorId;

    private String evaluator;

    private Float marks;

    private Boolean hasConductedEvaluation;

}
