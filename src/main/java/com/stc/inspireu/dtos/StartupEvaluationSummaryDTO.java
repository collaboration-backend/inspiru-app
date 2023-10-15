package com.stc.inspireu.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StartupEvaluationSummaryDTO {

    private Long applicationId;

    private String startupName;

    private String phase;

    private List<StartupEvaluationSummaryEvaluatorDTO> evaluators = new ArrayList<>();

}
