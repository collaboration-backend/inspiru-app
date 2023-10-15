package com.stc.inspireu.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.stc.inspireu.models.ProgressReport;
import com.stc.inspireu.models.Startup;

@Transactional
public interface ProgessReportRepository
    extends JpaRepository<ProgressReport, Long>, JpaSpecificationExecutor<ProgressReport> {

    public Page<ProgressReport> findAll(Specification<ProgressReport> spec, Pageable pageable);

    ProgressReport findByStartup_IdAndMonthAndYear(Long startupId, Integer month, Integer year);

    @Transactional
    public List<ProgressReport> findByStartup_Id(Long id);

    public ProgressReport findByStartup_IdAndYearAndMonth(Long id, Integer year, Integer month);

    public Optional<ProgressReport> findByIdAndStartup_IdAndYearAndMonth(Long progressReportId, Startup startup,
                                                                         Integer year, Integer month);

    public Optional<ProgressReport> findByIdAndStartup_IdAndYearAndMonth(Long progressReportId, Long id, Integer year,
                                                                         Integer month);

    @Query("select u.id as id, u.month as month, u.fundraiseInvestment as fundraiseInvestment from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndFundraiseInvestment(Long startupId);

    @Query("select u.id as id, u.month as month, u.marketValue as marketValue from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndMarketValue(Long startupId);

    @Query("select u.id as id, u.month as month, u.sales as sales from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndSales(Long startupId);

    @Query("select u.id as id, u.month as month, u.salesExpected as salesExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndSalesEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.revenue as revenue from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndRevenue(Long startupId);

    @Query("select u.id as id, u.month as month, u.revenueExpected as revenueExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndRevenueEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.loans as loans from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndLoans(Long startupId);

    @Query("select u.id as id, u.month as month, u.fteEmployees as fteEmployees from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndFteEmployees(Long startupId);

    @Query("select u.id as id, u.month as month, u.fteEmployeesExpected as fteEmployeesExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndFteEmployeesEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.pteEmployees as pteEmployees from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndPteEmployees(Long startupId);

    @Query("select u.id as id, u.month as month, u.pteEmployeesExpected as pteEmployeesExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndPteEmployeesEx(Long startupId);

    @Transactional
    @Modifying // to mark delete or update query
    @Query(value = "DELETE FROM ProgressReport e WHERE e.id = :ProgressReportId")
    int removeProgressReportById(@Param("ProgressReportId") Long ProgressReportId);

    @Modifying
    @Query("UPDATE ProgressReport c SET c.status = :status WHERE c.intakeProgram.id = :intakeProgramId")
    void updateStatus(@Param("intakeProgramId") Long intakeProgramId, @Param("status") String status);

    public List<ProgressReport> findByIntakeProgram_IdAndStartupIdIsNull(Long intakeProgramId);

    public Optional<ProgressReport> findByIdAndIntakeProgram_Id(Long id, Long intakePgmId);

    public List<ProgressReport> findByIntakeProgram_IdAndStatusAndStartupIdIsNull(Long long1, String string);

    @Query("select a from ProgressReport a where a.intakeProgram.id = :long1 and a.status =:string and a.startup.id = :startupId  and  a.modifiedOn <= :before")
    public List<ProgressReport> findByIntakeProgram_IdAndStatusAndStartupIdAndUpdatedAtBefore(Long long1, String string,
                                                                                              Long startupId, Date before);

    public Page<ProgressReport> findByStartupIdIsNull(Pageable paging);

    public Page<ProgressReport> findByIntakeProgram_IdAndStartupIdNotNull(Long intakeProgramId, Pageable paging);

    @Query("select u.id as id, u.month as month, u.freelancers as freelancers from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndFreelancers(Long startupId);

    @Query("select u.id as id, u.month as month, u.freelancersExpected as freelancersExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndFreelancersEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.users as users from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndCustomers(Long startupId);

    @Query("select u.id as id, u.month as month, u.usersExpected as usersExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndCustomersEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.profitLoss as profitLoss from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndProfitOrLoss(Long startupId);

    @Query("select u.id as id, u.month as month, u.profitLossExpected as profitLossExpected from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndProfitOrLossEx(Long startupId);

    @Query("select u.id as id, u.month as month, u.highGrossMerchandise as highGrossMerchandise from ProgressReport u where u.startup.id = :startupId order by u.createdOn ASC")
    public List<Map<String, Object>> getByStartupAndHighGrossMerchandise(Long startupId);

    @Query("SELECT SUM(m.marketValue) FROM ProgressReport m")
    public Integer sumMarketValue();

    @Query("SELECT SUM(m.fundraiseInvestment) FROM ProgressReport m")
    public Integer sumFundraiser();

    public Page<ProgressReport> findByIntakeProgram_IdAndStartupIdNotNullAndStartup_StartupNameContainingIgnoreCase(
        Long intakeProgramId, String filterKeyword, Pageable paging);

    @Query("select u from ProgressReport u where u.intakeProgram.id = :intakeProgramId and u.submittedUser is not null and lower(u.submittedUser.alias) like %:filterKeyword%")
    public Page<ProgressReport> getPRByKeyword(Long intakeProgramId, String filterKeyword, Pageable paging);

    public Page<ProgressReport> findByReportNameContainingIgnoreCaseAndIntakeProgram_IdAndStartupIdNotNull(
        String reportName, Long intakeProgramId, Pageable pageable);

    public List<ProgressReport> findByIntakeProgram_IdAndStartup_IdAndYearOrderByCreatedOnDesc(Long id, Long id2,
                                                                                               int year, Pageable top2);

    public List<ProgressReport> findByIntakeProgram_IdAndStartup_IdAndYearAndStatusOrderByCreatedOnDesc(Long id,
                                                                                                        Long id2, int year, String string, Pageable top2);

    List<ProgressReport> findByIntakeProgram_IdAndReportName(Long intakePgmId, String reportName);

    boolean existsByIntakeProgram_IdAndReportName(Long intakePgmId, String reportName);
}
