package com.stc.inspireu.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PutProgressReportDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "must be a valid month")
    @Max(value = 12, message = "must be a valid month")
    private Integer month;

    @Min(value = 2021, message = "must be a valid year")
    @Max(value = 2100, message = "must be a valid year")
    private Integer year;

    @NotBlank(message = "jsonForm required")
    private String jsonForm;

    private String jsonFormValue;

    @NotNull(message = "isSubmitted required")
    private Boolean isSubmitted;

    private MultipartFile[] files;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer fundraiseInvestment;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer marketValue;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer sales;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer salesExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer revenue;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer revenueExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer loans;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer fteEmployees;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer fteEmployeesExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer pteEmployees;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer pteEmployeesExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer freelancers;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer freelancersExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer users;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer usersExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer profitLoss;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer profitLossExpected;

    @Min(value = 0, message = "must be a valid number")
    @Max(value = Integer.MAX_VALUE, message = "must be a valid number")
    private Integer highGrossMerchandise;
}
