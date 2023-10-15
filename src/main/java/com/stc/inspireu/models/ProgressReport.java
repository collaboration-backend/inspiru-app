package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "progress_reports")
public class ProgressReport extends BaseEntity {

    @Column
    private String reportName;

    @Column
    private String status;

    @Column
    private Integer month;

    @Column
    private Integer year;

    @Column(columnDefinition = "TEXT")
    private String jsonReportDetail;

    @Column
    private String jsonFormValue;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "progressReportFilesId")
    private List<ProgressReportFile> progressReportFiles;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "submittedUserId")
    private User submittedUser;

    @Column(nullable = false)
    private Integer fundraiseInvestment;

    @Column(nullable = false)
    private Integer marketValue;

    @Column(nullable = false)
    private Integer sales;
    @Column(nullable = false)
    private Integer salesExpected;

    @Column(nullable = false)
    private Integer revenue;
    @Column(nullable = false)
    private Integer revenueExpected;

    @Column(nullable = false)
    private Integer loans;

    @Column(nullable = false)
    private Integer fteEmployees;
    @Column(nullable = false)
    private Integer fteEmployeesExpected;

    @Column(nullable = false)
    private Integer pteEmployees;
    @Column(nullable = false)
    private Integer pteEmployeesExpected;

    @Column(nullable = false)
    private Integer freelancers;
    @Column(nullable = false)
    private Integer freelancersExpected;

    @Column(nullable = false)
    private Integer users;
    @Column(nullable = false)
    private Integer usersExpected;

    @Column(nullable = false)
    private Integer profitLoss;
    @Column(nullable = false)
    private Integer profitLossExpected;

    @Column(nullable = false)
    private Integer highGrossMerchandise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intakeProgramId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeProgram intakeProgram;

    @Column
    private Long refProgressReportId;
}
