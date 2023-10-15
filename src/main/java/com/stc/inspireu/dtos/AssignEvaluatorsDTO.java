package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class AssignEvaluatorsDTO {

    @NotEmpty(message = "Application is required")
    private List<Long> applicationIds;

    @NotEmpty(message = "Evaluator is required")
    @Size(max = 5, message = "Up to 5 evaluators are allowed")
    private List<Long> evaluatorIds;

}
