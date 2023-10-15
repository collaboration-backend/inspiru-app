package com.stc.inspireu.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class EvaluatorMarksDTO implements Serializable {

    private Long evaluatorId;

    private String evaluator;

    private Date evaluationCompletedOn;

    private float marks;

    public EvaluatorMarksDTO(Long evaluatorId, String evaluator, float marks) {
        this.evaluatorId = evaluatorId;
        this.evaluator = evaluator;
        this.marks = marks;
        this.evaluationCompletedOn = new Date();
    }
}
